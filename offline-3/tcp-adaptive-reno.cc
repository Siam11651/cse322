/*
 * Copyright (c) 2013 ResiliNets, ITTC, University of Kansas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Authors: Siddharth Gangadhar <siddharth@ittc.ku.edu>,
 *          Truc Anh N. Nguyen <annguyen@ittc.ku.edu>,
 *          Greeshma Umapathi
 *
 * James P.G. Sterbenz <jpgs@ittc.ku.edu>, director
 * ResiliNets Research Group  https://resilinets.org/
 * Information and Telecommunication Technology Center (ITTC)
 * and Department of Electrical Engineering and Computer Science
 * The University of Kansas Lawrence, KS USA.
 *
 * Work supported in part by NSF FIND (Future Internet Design) Program
 * under grant CNS-0626918 (Postmodern Internet Architecture),
 * NSF grant CNS-1050226 (Multilayer Network Resilience Analysis and Experimentation on GENI),
 * US Department of Defense (DoD), and ITTC at The University of Kansas.
 */

#include "tcp-adaptive-reno.h"

#include "ns3/log.h"
#include "ns3/simulator.h"

NS_LOG_COMPONENT_DEFINE("TcpAdaptiveReno");

namespace ns3
{

NS_OBJECT_ENSURE_REGISTERED(TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId (void)
{
  static TypeId tid = TypeId("ns3::TcpAdaptiveReno")
    .SetParent<TcpWestwoodPlus>()
    .SetGroupName ("Internet")
    .AddConstructor<TcpAdaptiveReno>()
  ;
  return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno()
    : TcpWestwoodPlus(),
      m_rtt_current(0),
      m_rtt_cong(0),
      m_rtt_min(0),
      m_rtt_packet_loss(0),
      m_rtt_cong_prev(0),
      m_increase_window(0),
      m_base_window(0),
      m_probe_window(0)
{
    NS_LOG_FUNCTION(this);
}

TcpAdaptiveReno::TcpAdaptiveReno(const TcpAdaptiveReno& sock)
    : TcpWestwoodPlus(sock),
      m_rtt_current(0),
      m_rtt_cong(0),
      m_rtt_min(0),
      m_rtt_packet_loss(0),
      m_rtt_cong_prev(0),
      m_increase_window(0),
      m_base_window(0),
      m_probe_window(0)
{
    NS_LOG_FUNCTION(this);
    NS_LOG_LOGIC("Invoked the copy constructor");
}

TcpAdaptiveReno::~TcpAdaptiveReno()
{
}

void
TcpAdaptiveReno::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{
    NS_LOG_FUNCTION(this << tcb << packetsAcked << rtt);

    if (rtt.IsZero())
    {
        NS_LOG_WARN("RTT measured is zero!");
        return;
    }

    m_ackedSegments += packetsAcked;

    if(m_rtt_min.IsZero())
    {
        m_rtt_min = rtt;
    }
    else if(rtt < m_rtt_min)
    {
        m_rtt_min = rtt;
    }

    m_rtt_current = rtt;

    NS_LOG_LOGIC("Min RTT is : " << m_rtt_min.GetMilliSeconds() << "ms");
    NS_LOG_LOGIC ("CurRtt: " << m_rtt_current.GetMilliSeconds () << "ms");
    EstimateBW(rtt, tcb);
}

void
TcpAdaptiveReno::EstimateBW(const Time& rtt, Ptr<TcpSocketState> tcb)
{
    NS_LOG_FUNCTION(this);

    NS_ASSERT(!rtt.IsZero());

    m_currentBW = DataRate(m_ackedSegments * tcb->m_segmentSize * 8.0 / rtt.GetSeconds());
    m_IsCount = false;

    m_ackedSegments = 0;

    NS_LOG_LOGIC("Estimated BW: " << m_currentBW);

    // Filter the BW sample

    constexpr double ALPHA = 0.9;

    if (m_fType == TcpAdaptiveReno::TUSTIN)
    {
        DataRate sample_bwe = m_currentBW;
        m_currentBW = (m_lastBW * ALPHA) + (((sample_bwe + m_lastSampleBW) * 0.5) * (1 - ALPHA));
        m_lastSampleBW = sample_bwe;
        m_lastBW = m_currentBW;
    }

    NS_LOG_LOGIC("Estimated BW after filtering: " << m_currentBW);
}

double_t
TcpAdaptiveReno::EstimateCongestionLevel()
{
    double_t alpha_use = ALPHA;

    if(m_rtt_cong_prev < m_rtt_min)
    {
        alpha_use = 0.0;
    }

    double_t rtt_cong_seconds = alpha_use * m_rtt_cong_prev.GetSeconds() + (1 - alpha_use) * m_rtt_packet_loss.GetSeconds();
    m_rtt_cong = ns3::Seconds(rtt_cong_seconds);

    NS_LOG_LOGIC("cong rtt : " << m_rtt_cong << " ; m_rtt_cong_prev : " << m_rtt_cong_prev << " ; rtt packet loss : " << m_rtt_packet_loss);

    return std::min((m_rtt_current.GetSeconds() - m_rtt_min.GetSeconds()) / (m_rtt_cong.GetSeconds() - m_rtt_min.GetSeconds()), 1.0);
}

void
TcpAdaptiveReno::EstimateIncWnd(ns3::Ptr<ns3::TcpSocketState> tcb)
{
    double_t congestion_level = EstimateCongestionLevel();
    double_t max_increase_window = (m_currentBW.Get().GetBitRate() * std::pow(tcb->m_segmentSize, 2.0)) / SCALING_FACTOR / 8.0;
    double_t alpha = 10.0;
    double_t beta = 2.0 * max_increase_window * (1.0 / alpha - (1.0 / alpha + 1.0) / std::exp(alpha));
    double_t gamma = 1.0 - 2.0 * max_increase_window * (1.0 / alpha - (1.0 / alpha + 0.5) / std::exp(alpha));
    m_increase_window = (max_increase_window / std::exp(congestion_level * alpha)) +  congestion_level * beta + gamma;

    NS_LOG_LOGIC("MinRtt: " << m_rtt_min.GetMilliSeconds() << "ms");
    NS_LOG_LOGIC("ConjRtt: " << m_rtt_cong.GetMilliSeconds() << "ms");
    NS_LOG_LOGIC("max_increase_window: " << max_increase_window <<"; congestion: " << congestion_level << " ; beta: " << beta <<" ; gamma: "<< gamma << " ; exp(alpha * congestion): " << std::exp(alpha * congestion_level));
    NS_LOG_LOGIC("m_increase_window: " << m_increase_window << " ; prev_wind: " << tcb->m_cWnd << " ; new: " << (m_increase_window / tcb->m_cWnd));
    NS_LOG_LOGIC("Congestion level: " << congestion_level);
    NS_LOG_LOGIC("Increment Window: " << m_increase_window);
}

uint32_t
TcpAdaptiveReno::GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight [[maybe_unused]])
{
    m_rtt_cong_prev = m_rtt_cong;
    m_rtt_packet_loss = m_rtt_current;
    double_t congestion_level = EstimateCongestionLevel();
    double_t ss_thresh = std::max(2.0 * tcb->m_segmentSize, tcb->m_cWnd.Get() / (1.0 + congestion_level));
    m_base_window = ss_thresh;
    m_probe_window = 0;

    NS_LOG_LOGIC("new ssthresh : " << ss_thresh <<" ; old cong rtt : "<< m_rtt_cong_prev <<" ; new cong rtt : " << m_rtt_cong << " ; cong : " << congestion_level);

    return ss_thresh;
}

/**
 * \brief NewReno congestion avoidance
 *
 * During congestion avoidance, cwnd is incremented by roughly 1 full-sized
 * segment per round-trip time (RTT).
 *
 * \param tcb internal congestion state
 * \param segmentsAcked count of segments acked
 */
void
TcpAdaptiveReno::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    NS_LOG_FUNCTION(this << tcb << segmentsAcked);

    if (segmentsAcked > 0)
    {
        EstimateIncWnd(tcb);

        m_base_window += std::max(1.0, std::pow(tcb->m_segmentSize, 2.0) / tcb->m_cWnd.Get());
        m_probe_window = std::max(m_probe_window + m_increase_window / tcb->m_cWnd.Get(), 0.0);

        NS_LOG_LOGIC("Before " << tcb->m_cWnd << " ; base " << m_base_window <<" ; probe " << m_probe_window);

        tcb->m_cWnd.Set(m_base_window + m_probe_window);

        NS_LOG_INFO("In CongAvoid, updated to cwnd " << tcb->m_cWnd << " ssthresh " << tcb->m_ssThresh);
    }
}

} // namespace ns3

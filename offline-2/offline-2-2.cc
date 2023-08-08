#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/ssid.h"
#include "ns3/mobility-module.h"
#include "ns3/propagation-loss-model.h"
#include "ns3/wifi-net-device.h"

#define WIRED_SPEED "1Mbps"
#define WIRELESS_SPEED "5Mbps"
#define PACKET_SIZE 1024
#define TX_RANGE 5
#define DELAY 100 // milliseconds

// topology lol
// s                    r
// s    ap0 ---- ap1    r
// s                    r

NS_LOG_COMPONENT_DEFINE("offline-2-2");

uint64_t start_time = 0;
uint64_t packet_sent = 0;
uint64_t packet_recieved = 0;
uint64_t bits_sent = 0;
uint64_t bits_recieved = 0;
double_t throughput = 0.0;
double_t ratio = 0.0;

void packet_sent_counter(ns3::Ptr<const ns3::Packet> packet_ptr)
{
    ++packet_sent;
    bits_sent += packet_ptr->GetSize() * 8;
}

void packet_recieved_counter(ns3::Ptr<const ns3::Packet> packet_ptr, const ns3::Address &address)
{
    ++packet_recieved;
    bits_recieved += packet_ptr->GetSize();
}

ns3::Ptr<ns3::Node> n1;

void timer()
{
    uint64_t time_elapsed = ns3::Simulator::Now().GetMilliSeconds();
    throughput = (double_t)bits_recieved / time_elapsed;
    ratio = (double_t)packet_recieved / packet_sent;

    // ns3::Vector position = n1->GetObject<ns3::MobilityModel>()->GetPosition();

    // std::cout << position.x << " " << position.y << std::endl;

    ns3::Simulator::Schedule(ns3::MilliSeconds(DELAY), timer);
}

int main(int argc, char* argv[])
{
    uint64_t count_stations = 20;
    uint64_t count_flows = 10;
    uint64_t packet_rate = 100;
    uint64_t speed = 5;
    bool verbose = false;

    ns3::CommandLine cmd(__FILE__);

    cmd.Parse(argc, argv);
    cmd.AddValue("count-stations", "Set number of sender and reciever stations", count_stations);
    cmd.AddValue("count-flows", "Set number of data packets to be sent", count_flows);
    cmd.AddValue("packet-rate", "Set number of packets to be sent per second", packet_rate);
    cmd.AddValue("speed", "Set speed of nodes", speed);
    ns3::Time::SetResolution(ns3::Time::NS);

    if(verbose)
    {
        ns3::LogComponentEnable("OnOffApplication", ns3::LOG_LEVEL_INFO);
        ns3::LogComponentEnable("PacketSink", ns3::LOG_LEVEL_INFO);
    }

    ns3::Config::SetDefault("ns3::TcpSocket::SegmentSize", ns3::UintegerValue(PACKET_SIZE));

    ns3::NodeContainer access_point_nodes;
    ns3::NodeContainer left_nodes;
    ns3::NodeContainer right_nodes;

    access_point_nodes.Create(2);
    left_nodes.Create(count_stations / 2);
    right_nodes.Create(count_stations / 2);

    n1 = left_nodes.Get(0);

    ns3::PointToPointHelper p2p_helper;

    p2p_helper.SetDeviceAttribute("DataRate", ns3::StringValue("5Mbps"));
    p2p_helper.SetChannelAttribute("Delay", ns3::StringValue("2ms"));

    ns3::NetDeviceContainer access_points_net_devices = p2p_helper.Install(access_point_nodes);

    ns3::YansWifiChannelHelper yans_wifi_channel_helper = ns3::YansWifiChannelHelper::Default();
    ns3::YansWifiPhyHelper left_yans_wifi_phy_helper;
    ns3::YansWifiPhyHelper right_yans_wifi_phy_helper;

    left_yans_wifi_phy_helper.SetChannel(yans_wifi_channel_helper.Create());
    right_yans_wifi_phy_helper.SetChannel(yans_wifi_channel_helper.Create());

    ns3::Ssid left_ssid("left-ssid");
    ns3::Ssid right_ssid("right-ssid");
    ns3::WifiMacHelper wifi_mac_helper;
    ns3::WifiHelper wifi_helper;

    wifi_mac_helper.SetType("ns3::StaWifiMac", "Ssid", ns3::SsidValue(left_ssid), "ActiveProbing", ns3::BooleanValue(false));
    
    ns3::NetDeviceContainer left_station_net_devices = wifi_helper.Install(left_yans_wifi_phy_helper, wifi_mac_helper, left_nodes);
    
    wifi_mac_helper.SetType("ns3::ApWifiMac", "Ssid", ns3::SsidValue(left_ssid));

    ns3::NetDeviceContainer left_access_point_net_devices = wifi_helper.Install(left_yans_wifi_phy_helper, wifi_mac_helper, access_point_nodes.Get(0));
    
    wifi_mac_helper.SetType("ns3::StaWifiMac", "Ssid", ns3::SsidValue(right_ssid), "ActiveProbing", ns3::BooleanValue(false));
    
    ns3::NetDeviceContainer right_station_net_devices = wifi_helper.Install(right_yans_wifi_phy_helper, wifi_mac_helper, right_nodes);
    
    wifi_mac_helper.SetType("ns3::ApWifiMac", "Ssid", ns3::SsidValue(right_ssid));

    ns3::NetDeviceContainer right_access_point_net_devices = wifi_helper.Install(right_yans_wifi_phy_helper, wifi_mac_helper, access_point_nodes.Get(1));
    ns3::MobilityHelper mobility_helper;

    mobility_helper.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility_helper.Install(access_point_nodes);

    ns3::Ptr<ns3::ConstantRandomVariable> constant_random_variable_ptr = ns3::Create<ns3::ConstantRandomVariable>();

    constant_random_variable_ptr->SetAttribute("Constant", ns3::DoubleValue((double)speed));

    // mobility_helper.SetMobilityModel("ns3::RandomDirection2dMobilityModel", "Bounds", ns3::RectangleValue(ns3::Rectangle(0, 0, count_stations, count_stations)), "Speed", ns3::PointerValue(constant_random_variable_ptr));
    mobility_helper.Install(left_nodes);
    mobility_helper.Install(right_nodes);

    ns3::InternetStackHelper internet_stack_helper;

    internet_stack_helper.Install(left_nodes);
    internet_stack_helper.Install(right_nodes);
    internet_stack_helper.Install(access_point_nodes);

    ns3::Ipv4AddressHelper ipv4_address_helper;

    ipv4_address_helper.SetBase("10.1.1.0", "255.255.255.0");
    ipv4_address_helper.Assign(access_points_net_devices);
    ipv4_address_helper.SetBase("10.1.2.0", "255.255.255.0");
    ipv4_address_helper.Assign(left_access_point_net_devices);
    ipv4_address_helper.Assign(left_station_net_devices);
    ipv4_address_helper.SetBase("10.1.3.0", "255.255.255.0");
    ipv4_address_helper.Assign(right_access_point_net_devices);

    ns3::Ipv4InterfaceContainer right_station_interfaces = ipv4_address_helper.Assign(right_station_net_devices);
    ns3::ApplicationContainer sender_apps;
    ns3::ApplicationContainer reciever_apps;

    for(size_t i = 0; i < count_flows; ++i)
    {
        ns3::OnOffHelper sender_helper("ns3::TcpSocketFactory", ns3::InetSocketAddress(right_station_interfaces.GetAddress(i % (count_stations / 2)), 9));

        sender_helper.SetAttribute("PacketSize", ns3::UintegerValue(PACKET_SIZE));
        sender_helper.SetAttribute("DataRate", ns3::DataRateValue(ns3::DataRate(packet_rate * PACKET_SIZE)));
        sender_apps.Add(sender_helper.Install(left_nodes.Get(i % (count_stations / 2))));
    }

    for(size_t i = 0; i < count_stations / 2; ++i)
    {
        ns3::PacketSinkHelper reciever_helper("ns3::TcpSocketFactory", ns3::InetSocketAddress(ns3::Ipv4Address::GetAny(), +9));

        reciever_apps.Add(reciever_helper.Install(right_nodes.Get(i)));
    }

    for(size_t i = 0; i < sender_apps.GetN(); ++i)
    {
        sender_apps.Get(i)->TraceConnectWithoutContext("Tx", ns3::MakeCallback(packet_sent_counter));
    }

    for(size_t i = 0; i < reciever_apps.GetN(); ++i)
    {
        reciever_apps.Get(i)->TraceConnectWithoutContext("Rx", ns3::MakeCallback(packet_recieved_counter));
    }

    sender_apps.Start(ns3::Seconds(1));
    reciever_apps.Start(ns3::Seconds(0));
    ns3::Ipv4GlobalRoutingHelper::PopulateRoutingTables();
    ns3::Simulator::Stop(ns3::Seconds(10));
    ns3::Simulator::Schedule(ns3::MilliSeconds(DELAY), timer);
    ns3::Simulator::Run();
    ns3::Simulator::Destroy();

    return 0;
}

#include "bit.hpp"

offline4::bit::bit()
{
    m_value = false;
    m_color = offline4::bit::color::DEFAULT;
}

offline4::bit::bit(const bool &value, const offline4::bit::color &color)
{
    m_value = value;
    m_color = color;
}

void offline4::bit::set_value(const bool &value)
{
    m_value = value;
}

void offline4::bit::set_color(const offline4::bit::color &color)
{
    m_color = color;
}

bool offline4::bit::get_value() const
{
    return m_value;
}

offline4::bit::color offline4::bit::get_color() const
{
    return m_color;
}
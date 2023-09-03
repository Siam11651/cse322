#include "bit.hpp"

std::map<offline4::bit::color, std::string> offline4::bit::color_to_code_mapper =
{
    {offline4::bit::color::none, "\033[0m"},
    {offline4::bit::color::red, "\033[31m"},
    {offline4::bit::color::green, "\033[32m"},
    {offline4::bit::color::blue, "\033[36m"}
};

offline4::bit::bit()
{
    m_value = false;
    m_color = offline4::bit::color::none;
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

std::strong_ordering offline4::bit::operator <=> (const offline4::bit &other) const
{
    if(m_value < other.m_value)
    {
        return std::strong_ordering::less;
    }
    else if(m_value > other.m_value)
    {
        return std::strong_ordering::greater;
    }
    else
    {
        return std::strong_ordering::equal;
    }
}

std::ostream &offline4::operator << (std::ostream &ostream, const offline4::bit &bit)
{
    ostream << offline4::bit::color_to_code_mapper.at(bit.get_color()) << bit.get_value() << offline4::bit::color_to_code_mapper.at(offline4::bit::color::none);

    return ostream;
}
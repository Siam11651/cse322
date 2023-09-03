#ifndef BIT_H
#define BIT_H

#include <ostream>
#include <map>

namespace offline4
{
    class bit
    {
    public:
        enum class color
        {
            none, red, green, blue
        };

    private:
        bool m_value;
        offline4::bit::color m_color;

        static std::map<offline4::bit::color, std::string> color_to_code_mapper;

    public:
        bit();
        bit(const bool &value, const offline4::bit::color &color = offline4::bit::color::none);
        void set_value(const bool &value);
        void set_color(const offline4::bit::color &color);
        bool get_value() const;
        offline4::bit::color get_color() const;
        std::strong_ordering operator <=> (const offline4::bit &other) const;
        friend std::ostream &operator << (std::ostream &ostream, const offline4::bit &bit);
    };

    std::ostream &operator << (std::ostream &ostream, const offline4::bit &bit);
}

#endif
#ifndef BIT_H
#define BIT_H

namespace offline4
{
    class bit
    {
    public:
        enum class color
        {
            DEFAULT, RED, GREEN, BLUE
        };

    private:
        bool m_value;
        offline4::bit::color m_color;

    public:
        bit();
        bit(const bool &value, const offline4::bit::color &color = offline4::bit::color::DEFAULT);
        void set_value(const bool &value);
        void set_color(const offline4::bit::color &color);
        bool get_value() const;
        offline4::bit::color get_color() const;
    };
}

#endif
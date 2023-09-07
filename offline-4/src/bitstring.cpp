#include <exception>
#include <random>
#include <iostream>
#include "bitstring.hpp"

offline4::bitstring::bitstring(const uint64_t &size, const offline4::bit &value) : std::list<bit>(size, value)
{

}

offline4::bitstring::bitstring(const std::list<bool> &list)
{
    for(std::list<bool>::const_iterator iterator = list.begin(); iterator != list.end(); ++iterator)
    {
        push_back(offline4::bit(*iterator));
    }
}

offline4::bitstring offline4::bitstring::trim() const
{
    offline4::bitstring to_return;
    bool found = false;

    for(offline4::bitstring::const_iterator iterator = begin(); iterator != end(); ++iterator)
    {
        if(iterator->get_value())
        {
            found = true;
        }

        if(found)
        {
            to_return.push_back(*iterator);
        }
    }

    if(to_return.empty())
    {
        to_return.push_back(offline4::bit());
    }

    return to_return;
}

offline4::bitstring offline4::bitstring::distort(const double_t &probability) const
{
    offline4::bitstring to_return;
    constexpr uint64_t limit = 1000;
    std::random_device random_device_engine;

    for(offline4::bitstring::const_iterator iterator = begin(); iterator != end(); ++iterator)
    {
        uint64_t value = random_device_engine() % limit;

        if(value < (uint64_t)(limit * probability))
        {
            to_return.push_back(offline4::bit(!iterator->get_value(), offline4::bit::color::red));
        }
        else
        {
            to_return.push_back(offline4::bit(iterator->get_value()));
        }
    }

    return to_return;
}

std::strong_ordering offline4::bitstring::operator <=> (const offline4::bitstring &other) const
{
    offline4::bitstring left = this->trim();
    offline4::bitstring right = other.trim();

    if(left.size() < right.size())
    {
        return std::strong_ordering::less;
    }
    else if(left.size() > right.size())
    {
        return std::strong_ordering::greater;
    }

    offline4::bitstring::const_iterator left_iterator = left.begin();
    offline4::bitstring::const_iterator right_iterator = right.begin();

    while(left_iterator != left.end() && right_iterator != right.end()) // altho, they supposed to be inequal at the same time
    {
        if(*left_iterator != *right_iterator)
        {
            if(*left_iterator < *right_iterator)
            {
                return std::strong_ordering::less;
            }
            else
            {
                return std::strong_ordering::greater;
            }
        }

        ++left_iterator;
        ++right_iterator;
    }

    return std::strong_ordering::equal;
}

bool offline4::bitstring::operator == (const offline4::bitstring &other) const
{
    return (*this <=> other) == std::strong_ordering::equal;
}

offline4::bitstring offline4::bitstring::operator ^ (const offline4::bitstring &other) const
{
    offline4::bitstring left(*this);
    offline4::bitstring right(other);

    while(left.size() < right.size())
    {
        left.push_front(false);
    }

    while(left.size() > right.size())
    {
        right.push_front(false);
    }

    offline4::bitstring to_return;

    offline4::bitstring::const_iterator left_iterator = left.begin();
    offline4::bitstring::const_iterator right_iterator = right.begin();

    for(; left_iterator != left.end(); ++left_iterator, ++right_iterator)
    {
        to_return.push_back(left_iterator->get_value() ^ right_iterator->get_value());
    }

    return to_return;
}

offline4::bitstring offline4::bitstring::operator / (const offline4::bitstring &other) const
{
    if(other == offline4::bitstring({false}))
    {
        throw std::logic_error("Divide by zero");
    }

    if(other > *this)
    {
        return offline4::bitstring({false});
    }

    offline4::bitstring divisor = other.trim();
    offline4::bitstring::const_iterator dividend_iterator = this->begin();
    offline4::bitstring dividend_part;

    for(size_t j = 0; j < divisor.size(); ++j, ++dividend_iterator)
    {
        dividend_part.push_back(*dividend_iterator);
    }

    offline4::bitstring to_return;

    for(size_t i = divisor.size(); i < this->size(); ++i, ++dividend_iterator)
    {
        if(dividend_part < divisor)
        {
            dividend_part = dividend_part ^ offline4::bitstring({false});

            to_return.push_back(offline4::bit(false));
        }
        else
        {
            dividend_part = dividend_part ^ divisor;

            to_return.push_back(offline4::bit(true));
        }

        dividend_part.pop_front();
        dividend_part.push_back(*dividend_iterator);
    }

    if(dividend_part < divisor)
    {
        dividend_part = dividend_part ^ offline4::bitstring({false});

        to_return.push_back(offline4::bit(false));
    }
    else
    {
        dividend_part = dividend_part ^ divisor;

        to_return.push_back(offline4::bit(true));
    }

    return to_return;
}

offline4::bitstring offline4::bitstring::operator % (const offline4::bitstring &other) const
{
    if(other == offline4::bitstring({false}))
    {
        throw std::logic_error("Divide by zero");
    }

    if(other > *this)
    {
        return offline4::bitstring({false});
    }

    offline4::bitstring divisor = other.trim();
    offline4::bitstring::const_iterator dividend_iterator = this->begin();
    offline4::bitstring dividend_part;

    for(size_t j = 0; j < divisor.size(); ++j, ++dividend_iterator)
    {
        dividend_part.push_back(*dividend_iterator);
    }

    for(size_t i = divisor.size(); i < this->size(); ++i, ++dividend_iterator)
    {
        if(dividend_part.front() < divisor.front())
        {
            dividend_part = dividend_part ^ offline4::bitstring({false});
        }
        else
        {
            dividend_part = dividend_part ^ divisor;
        }

        dividend_part.pop_front();
        dividend_part.push_back(*dividend_iterator);
    }

    if(dividend_part.front() < divisor.front())
    {
        dividend_part = dividend_part ^ offline4::bitstring({false});
    }
    else
    {
        dividend_part = dividend_part ^ divisor;
    }

    dividend_part.pop_front();

    return dividend_part;
}

std::ostream &offline4::operator << (std::ostream &ostream, const offline4::bitstring &bitblock)
{
    for(offline4::bitstring::const_iterator iterator = bitblock.begin(); iterator != bitblock.end(); ++iterator)
    {
        ostream << *iterator;
    }

    return ostream;
}
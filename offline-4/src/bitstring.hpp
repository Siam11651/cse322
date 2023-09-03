#ifndef BITSTRING_H
#define BITSTRING_H

#include <ostream>
#include <vector>
#include <list>
#include <string>
#include <cstdint>
#include <cmath>
#include "bit.hpp"

namespace offline4
{
    class bitstring : public std::list<bit>
    {
    public:
        bitstring(const uint64_t &size = 0, const offline4::bit &value = offline4::bit(false));
        bitstring(const std::list<bool> &list);
        offline4::bitstring trim() const;
        offline4::bitstring distort(const double_t &probability) const;
        std::strong_ordering operator <=> (const offline4::bitstring &other) const;
        bool operator == (const offline4::bitstring &other) const;
        offline4::bitstring operator ^ (const offline4::bitstring &other) const;
        offline4::bitstring operator / (const offline4::bitstring &other) const;
        offline4::bitstring operator % (const offline4::bitstring &other) const;
        friend std::ostream &operator << (std::ostream &ostream, const offline4::bitstring &bitblock);
    };

    std::ostream &operator << (std::ostream &ostream, const offline4::bitstring &bitblock);
}

#endif
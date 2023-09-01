#ifndef BITBLOCK_H
#define BITBLOCK_H

#include <ostream>
#include <map>
#include <vector>
#include <list>
#include <string>
#include <cstdint>
#include "bit.hpp"

namespace offline4
{
    class bitstring : public std::list<bit>
    {
    private:
        static std::map<offline4::bit::color, std::string> color_to_code_mapper;

    public:
        bitstring(const uint64_t &size = 0);
        friend std::ostream &operator << (std::ostream &ostream, const offline4::bitstring &bitblock);
    };

    std::ostream &operator << (std::ostream &ostream, const offline4::bitstring &bitblock);
}

#endif
#include "bitstring.hpp"

std::map<offline4::bit::color, std::string> offline4::bitstring::color_to_code_mapper =
{
    {offline4::bit::color::DEFAULT, "\033[0m"},
    {offline4::bit::color::RED, "\033[30m"},
    {offline4::bit::color::GREEN, "\033[32m"},
    {offline4::bit::color::BLUE, "\033[34m"}
};

offline4::bitstring::bitstring(const uint64_t &size) : std::list<bit>(size)
{

}

std::ostream &offline4::operator << (std::ostream &ostream, const offline4::bitstring &bitblock)
{
    for(offline4::bitstring::const_iterator iterator = bitblock.begin(); iterator != bitblock.end(); ++iterator)
    {
        ostream << offline4::bitstring::color_to_code_mapper.at(iterator->get_color()) << *iterator << offline4::bitstring::color_to_code_mapper.at(offline4::bit::color::DEFAULT);
    }

    return ostream;
}
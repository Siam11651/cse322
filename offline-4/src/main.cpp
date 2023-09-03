#include <iostream>
#include <cstdint>
#include <string>
#include <vector>
#include <bitset>
#include <cmath>
#include "bit.hpp"
#include "bitstring.hpp"

int main()
{
    std::string data_string;
    uint64_t number_of_bytes_per_row;
    std::string line;

    std::cout << "Enter data string: ";

    std::getline(std::cin, line);

    data_string = std::string(line);

    std::cout << "Enter number of bytes per row: ";

    std::getline(std::cin, line);

    number_of_bytes_per_row = std::stoul(line);

    std::cout << "Enter probability: ";

    std::getline(std::cin, line);

    double_t distortion_probability = std::stod(line);

    std::cout << "Enter generator polynomial: ";

    std::getline(std::cin, line);

    offline4::bitstring polynomial_bitstring;

    for(std::string::const_iterator iterator = line.begin(); iterator != line.end(); ++iterator)
    {
        if(*iterator == '1')
        {
            polynomial_bitstring.push_back(true);
        }
        else if(*iterator == '0')
        {
            polynomial_bitstring.push_back(false);
        }
    }

    if(data_string.size() % number_of_bytes_per_row != 0)
    {
        uint64_t pad_size = number_of_bytes_per_row - (data_string.size() % number_of_bytes_per_row);
        data_string += std::string(pad_size, '~');
    }

    std::cout << "Data string after padding: " << data_string << std::endl;

    std::vector<offline4::bitstring> blocks(data_string.size() / number_of_bytes_per_row, offline4::bitstring(number_of_bytes_per_row * 8));

    for(size_t i = 0; i < blocks.size(); ++i)
    {
        offline4::bitstring::iterator iterator = blocks[i].begin();

        for(size_t j = 0; j < number_of_bytes_per_row; ++j)
        {
            uint8_t byte = data_string[i * number_of_bytes_per_row + j];

            for(size_t k = 0; k < 8; ++k)
            {
                iterator->set_value(byte & (1 << (7 - k)));

                ++iterator;
            }
        }
    }

    std::cout << std::endl;
    std::cout << "Data blocks:" << std::endl;

    for(std::vector<offline4::bitstring>::const_iterator iterator = blocks.begin(); iterator != blocks.end(); ++iterator)
    {
        std::cout << *iterator << std::endl;
    }

    std::cout << std::endl;

    for(std::vector<offline4::bitstring>::iterator block_iterator = blocks.begin(); block_iterator != blocks.end(); ++block_iterator)
    {
        offline4::bitstring bitstring(*block_iterator);
        uint64_t count = 1;
        uint64_t shifter = 0;

        for(offline4::bitstring::const_iterator bit_iterator = block_iterator->begin(); bit_iterator != block_iterator->end(); ++bit_iterator)
        {
            if(count == (1 << shifter))
            {
                block_iterator->insert(bit_iterator, offline4::bit(false, offline4::bit::color::green));

                ++shifter;
                --bit_iterator;
            }

            ++count;
        }

        std::vector<bool> check_values((uint64_t)std::log2(block_iterator->size()) + 2, false);
        count = 0;
        shifter = 0;

        for(offline4::bitstring::const_iterator bit_iterator = block_iterator->begin(); bit_iterator != block_iterator->end(); ++bit_iterator)
        {
            ++count;
            uint64_t bit_masker = 0;

            if(count == (1 << shifter))
            {
                ++shifter;

                continue;
            }
            
            while((count >> bit_masker) != 0)
            {
                if(count & (1 << bit_masker))
                {
                    check_values[bit_masker] = check_values[bit_masker] ^ bit_iterator->get_value();
                }

                ++bit_masker;
            }
        }

        count = 1;
        shifter = 0;

        for(offline4::bitstring::iterator bit_iterator = block_iterator->begin(); bit_iterator != block_iterator->end(); ++bit_iterator)
        {
            if(count == (1 << shifter))
            {
                bit_iterator->set_value(check_values[shifter]);

                ++shifter;
            }

            ++count;
        }
    }

    std::cout << "Data block after adding check bits:" << std::endl;

    for(std::vector<offline4::bitstring>::const_iterator iterator = blocks.begin(); iterator != blocks.end(); ++iterator)
    {
        std::cout << *iterator << std::endl;
    }

    std::vector<std::vector<offline4::bit>> bit_matrix(blocks.size(), std::vector<offline4::bit>(blocks.front().size()));
    size_t i = 0;

    for(std::vector<offline4::bitstring>::const_iterator block_iterator = blocks.begin(); block_iterator != blocks.end(); ++block_iterator, ++i)
    {
        size_t j = 0;

        for(offline4::bitstring::const_iterator bit_iterator = block_iterator->begin(); bit_iterator != block_iterator->end(); ++bit_iterator, ++j)
        {
            bit_matrix[i][j] = *bit_iterator;
        }
    }

    std::cout << std::endl;

    offline4::bitstring serialized_bitstring;

    for(size_t i = 0; i < blocks.front().size(); ++i)
    {
        for(size_t j = 0; j < blocks.size(); ++j)
        {
            serialized_bitstring.push_back(offline4::bit(bit_matrix[j][i].get_value()));
        }
    }

    std::cout << "Data bits after column-wide serialization:" << std::endl;
    std::cout << serialized_bitstring << std::endl;
    std::cout << std::endl;

    offline4::bitstring dividend_bitstring(serialized_bitstring);

    dividend_bitstring.resize(serialized_bitstring.size() + polynomial_bitstring.size() - 1);

    offline4::bitstring checksum_bitstring = dividend_bitstring % polynomial_bitstring;
    offline4::bitstring appended_bitstring(serialized_bitstring);

    for(offline4::bitstring::iterator iterator = checksum_bitstring.begin(); iterator != checksum_bitstring.end(); ++iterator)
    {
        iterator->set_color(offline4::bit::color::blue);
        appended_bitstring.push_back(*iterator);
    }

    std::cout << "Data bits after appending CRC checksum:" << std::endl;
    std::cout << appended_bitstring << std::endl;
    std::cout << std::endl;

    offline4::bitstring distorted_bitstring = appended_bitstring.distort(distortion_probability);

    std::cout << "Recieved frame:" << std::endl;
    std::cout << distorted_bitstring << std::endl;
    std::cout << std::endl;

    offline4::bitstring error_detector_bitstring = distorted_bitstring % polynomial_bitstring;
    std::string verdict = "Error detected";

    if(error_detector_bitstring == offline4::bitstring({false}))
    {
        verdict = "No error detected";
    }

    std::cout << "Result of CRC checksum matching: " << verdict << std::endl;
    std::cout << std::endl;

    for(size_t i = 0; i < polynomial_bitstring.size() - 1; ++i)
    {
        distorted_bitstring.pop_back();
    }

    i = 0;
    size_t j = 0;

    for(offline4::bitstring::const_iterator iterator = distorted_bitstring.begin(); iterator != distorted_bitstring.end(); ++iterator)
    {
        bit_matrix[j][i] = *iterator;

        ++j;

        if(j == blocks.size())
        {
            j = 0;
            ++i;
        }
    }

    for(size_t i = 0; i < blocks.size(); ++i)
    {
        offline4::bitstring bitstring;

        for(std::vector<offline4::bit>::const_iterator iterator = bit_matrix[i].begin(); iterator != bit_matrix[i].end(); ++iterator)
        {
            bitstring.push_back(*iterator);
        }

        blocks[i] = bitstring;
    }

    std::cout << "Data blocks after removing CRC checksum bits:" << std::endl;

    for(std::vector<offline4::bitstring>::const_iterator iterator = blocks.begin(); iterator != blocks.end(); ++iterator)
    {
        std::cout << *iterator << std::endl;
    }

    std::cout << std::endl;

    return 0;
}
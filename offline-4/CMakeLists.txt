cmake_minimum_required(VERSION 3.20)
project(offline-4)

add_executable(offline-4
    "src/bit.cpp"
    "src/bitstring.cpp"
    "src/main.cpp"
)

set_property(TARGET offline-4 PROPERTY CXX_STANDARD 20)
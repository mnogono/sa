cmake_minimum_required(VERSION 3.9)
project(clion_test)

set(CMAKE_CXX_STANDARD 11)

add_executable(clion_test main.cpp analysis_tools.cpp analysis_tools.h sound_analysis_config.h)
cmake_minimum_required(VERSION 3.9)
project(clion_test)

set(CMAKE_CXX_STANDARD 11)

add_executable(
        clion_test
        main.cpp
        analysing_tools.cpp
        analysing_tools.h
        sound_analysis_config.h
        audio_analyser.cpp
        audio_analyser.h
        audio_local_maximum_aperture_info.cpp
        audio_local_maximum_aperture_info.h
)
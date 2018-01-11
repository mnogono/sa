cmake_minimum_required(VERSION 2.6)
project (sound-analysis)
set (SOUND_ANALYSIS_VERSION_MAJOR 1)
set (SOUND_ANALYSIS_VERSION_MINOR 0)
set (CMAKE_CXX_STANDARD 11)

add_executable(sound-analysis analysing_tools.cpp)

#add_custom_target(clean-cmake-files 
#  COMMAND ${CMAKE_COMMAND} -P clean-all.cmake
#)

#set(cmake_generated ${CMAKE_BINARY_DIR}/CMakeCache.txt
#                    ${CMAKE_BINARY_DIR}/cmake_install.cmake
#                    ${CMAKE_BINARY_DIR}/Makefile
#                    ${CMAKE_BINARY_DIR}/CMakeFiles
#)

#foreach(file ${cmake_generated})
#  if (EXISTS ${file})
#    file(REMOVE_RECURSE ${file})
#  endif()
#endforeach(file)

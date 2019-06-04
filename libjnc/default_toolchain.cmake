# CMAKE_SYSTEM_PROCESSOR from command line will be visible here
if (NOT SYSTEM_PROCESSOR)
    set(SYSTEM_PROCESSOR ${CMAKE_HOST_SYSTEM_PROCESSOR})
endif ()
string(REGEX REPLACE "^(([Xx]|[Ii][3-6])86|[Pp][Ee][Nn][Tt][Ii][Uu][Mm])$" "i386" SYSTEM_PROCESSOR ${SYSTEM_PROCESSOR})
string(REGEX REPLACE "^(([Xx](86[_-])?|[Aa][Mm][Dd])64|[Ee][Mm]64[Tt])$" "x86_64" SYSTEM_PROCESSOR ${SYSTEM_PROCESSOR})

if (((CMAKE_SYSTEM_NAME) AND ("${CMAKE_SYSTEM_NAME}" STREQUAL "Windows")) OR ((NOT CMAKE_SYSTEM_NAME) AND (${CMAKE_HOST_SYSTEM_NAME} STREQUAL Windows)))
    string(REGEX REPLACE "^i386-" "i686-" triple ${SYSTEM_PROCESSOR}-w64-mingw32)
    set(CMAKE_C_COMPILER ${triple}-gcc)
    set(CMAKE_C_COMPILER_TARGET ${SYSTEM_PROCESSOR}-pc-mingw32)
    set(CMAKE_CXX_COMPILER ${triple}-g++)
    set(CMAKE_CXX_COMPILER_TARGET ${SYSTEM_PROCESSOR}-pc-mingw32)
endif ()
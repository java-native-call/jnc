set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_SYSTEM_PROCESSOR ${ARCH})
set(CMAKE_C_COMPILER ${ARCH}-w64-mingw32-gcc)
set(CMAKE_CXX_COMPILER ${ARCH}-w64-mingw32-g++)
set(CMAKE_RC_COMPILER ${ARCH}-w64-mingw32-windres)
set(CMAKE_FIND_ROOT_PATH /usr/local/opt/mingw-w64/toolchain-${ARCH})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

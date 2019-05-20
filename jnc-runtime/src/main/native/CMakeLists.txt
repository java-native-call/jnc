cmake_minimum_required(VERSION 2.8.12)

if (NOT CMAKE_TOOLCHAIN_FILE)
    set(CMAKE_TOOLCHAIN_FILE default_toolchain.cmake)
endif ()

project(jnc C CXX)

if (NOT SYSTEM_PROCESSOR)
    set(SYSTEM_PROCESSOR ${CMAKE_SYSTEM_PROCESSOR})
endif ()

string(FIND "${SYSTEM_PROCESSOR}" 64 ARCH_BITS)
if (${ARCH_BITS} GREATER -1)
    set(ARCH_BITS 64)
else ()
    set(ARCH_BITS 32)
endif ()

if (${CMAKE_VERSION} VERSION_LESS 3.1)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
else ()
    set(CMAKE_CXX_STANDARD 11)
endif ()

file(GLOB SOURCES jni/*.h src/*.h src/*.c src/*.cpp)
add_library(${PROJECT_NAME} SHARED ${SOURCES})

include(ExternalProject)

set(SHARE_FLAGS "-Wall -Wextra -fPIC -fomit-frame-pointer -fno-ident")
set(LIBFFI_MAKE_CONFIG --enable-structs --enable-raw-api --enable-static --disable-shared --with-pic=yes)
set(LDFLAGS)
set(LIBFFI_BUILD_ENV
        "CC=${CMAKE_C_COMPILER}"
        "CXX=${CMAKE_CXX_COMPILER}"
        "AR=${CMAKE_AR}"
        "LD=${CMAKE_LINKER}"
        "RANLIB=${CMAKE_RANLIB}")

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)
    set(CMAKE_SHARED_LIBRARY_SUFFIX .jnilib)
    set(SHARE_FLAGS "${SHARE_FLAGS} -mmacosx-version-min=10.7")
    set(STRIP_ARGS -x)
else ()
    set(CMAKE_SHARED_LIBRARY_SUFFIX -${SYSTEM_PROCESSOR}${CMAKE_SHARED_LIBRARY_SUFFIX})
    set(SHARE_FLAGS "${SHARE_FLAGS} -m${ARCH_BITS}")
    set(STRIP_ARGS -s)
endif ()

if (${CMAKE_SYSTEM_NAME} STREQUAL Windows)
    if (${ARCH_BITS} EQUAL 32)
        set(LDFLAGS "${LDFLAGS} -Wl,--add-stdcall-alias -Wl,--kill-at")
    endif ()
    set(CMAKE_SHARED_LIBRARY_PREFIX "")
endif ()

set(SONAME ${CMAKE_SHARED_LIBRARY_PREFIX}${PROJECT_NAME}${CMAKE_SHARED_LIBRARY_SUFFIX})

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -install_name ${SONAME}")
    # INSTALL_NAME_DIR for cmake [2.8.12, 3.0)
    set_target_properties(${PROJECT_NAME} PROPERTIES MACOSX_RPATH "")
else ()
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-soname,${SONAME}")
endif ()

string(TOUPPER "${CMAKE_BUILD_TYPE}" BUILD_TYPE_UPPER)

set(LIBFFI_BUILD_CFLAGS "${CMAKE_C_FLAGS} ${SHARE_FLAGS} ${CMAKE_C_FLAGS_${BUILD_TYPE_UPPER}}")
set(LIBFFI_BUILD_CXXFLAGS "${CMAKE_CXX_FLAGS} ${SHARE_FLAGS} ${CMAKE_CXX_FLAGS_${BUILD_TYPE_UPPER}}")
set(LIBFFI_BUILD_LDFLAGS "${CMAKE_STATIC_LINKER_FLAGS} ${SHARE_FLAGS} ${LDFLAGS} ${CMAKE_STATIC_LINKER_FLAGS_${BUILD_TYPE_UPPER}}")

if (CMAKE_COMPILER_IS_GNUCC)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -static -static-libgcc")
endif ()

ExternalProject_Add(libffi
        GIT_REPOSITORY https://github.com/libffi/libffi.git GIT_TAG v3.3-rc0
        UPDATE_COMMAND true
        CONFIGURE_COMMAND true
        BUILD_COMMAND true
        INSTALL_COMMAND true
        WORKING_DIRECTORY <BINARY_DIR>)

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)

    set(LIB_FFI_A_ARCH)
    set(ARCH_FLAGS)

    foreach (arch i386 x86_64)
        ExternalProject_Add_Step(libffi configure-${arch}
                COMMAND ${CMAKE_COMMAND} -E make_directory <BINARY_DIR>-${arch}
                COMMAND ${CMAKE_COMMAND} -E chdir <BINARY_DIR>-${arch} env
                ${LIBFFI_BUILD_ENV}
                "CFLAGS=${LIBFFI_BUILD_CFLAGS} -arch ${arch}"
                "CXXFLAGS=${LIBFFI_BUILD_CXXFLAGS} -arch ${arch}"
                "LDFLAGS=${LIBFFI_BUILD_LDFLAGS} -arch ${arch}"
                # use relative path, let autoconf determine style of the path
                ../libffi/configure ${LIBFFI_MAKE_CONFIG}
                -host ${arch}-apple-darwin "--prefix=<INSTALL_DIR>"
                DEPENDEES autogen
                DEPENDERS configure)
        ExternalProject_Add_Step(libffi build-${arch}
                COMMAND $(MAKE)
                DEPENDEES configure-${arch}
                DEPENDERS build
                WORKING_DIRECTORY <BINARY_DIR>-${arch})
        set(LIB_FFI_A_ARCH ${LIB_FFI_A_ARCH} <BINARY_DIR>-${arch}/.libs/libffi.a)
        set(ARCH_FLAGS "${ARCH_FLAGS} -arch ${arch}")
    endforeach ()

    file(GLOB libffi-darwin-headers ${CMAKE_SOURCE_DIR}/darwin/include/*.h)

    ExternalProject_Add_Step(libffi copy-darwin-header
            COMMAND ${CMAKE_COMMAND} -E copy_directory ${CMAKE_SOURCE_DIR}/darwin <BINARY_DIR>
            DEPENDERS install
            DEPENDS ${libffi-darwin-headers})

    ExternalProject_Add_Step(libffi merge-static
            COMMAND ${CMAKE_COMMAND} -E make_directory <BINARY_DIR>/.libs
            COMMAND libtool -static -o <BINARY_DIR>/.libs/libffi.a
            ${LIB_FFI_A_ARCH}
            DEPENDEES build DEPENDERS install)

    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${ARCH_FLAGS}")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${ARCH_FLAGS}")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${ARCH_FLAGS}")
    set(LDFLAGS "${LDFLAGS} ${ARCH_FLAGS}")
else ()
    if (CMAKE_C_COMPILER_TARGET)
        set(HOST_CONFIG "--host" "${CMAKE_C_COMPILER_TARGET}")
    endif ()
    ExternalProject_Add_Step(libffi configure-impl
            COMMAND env ${LIBFFI_BUILD_ENV}
            "CFLAGS=${LIBFFI_BUILD_CFLAGS}"
            "CXXFLAGS=${LIBFFI_BUILD_CXXFLAGS}"
            "LDFLAGS=${LIBFFI_BUILD_LDFLAGS}"
            # use relative path, let autoconf determine style of the path
            ../libffi/configure ${LIBFFI_MAKE_CONFIG}
            ${HOST_CONFIG} "--prefix=<INSTALL_DIR>"
            DEPENDEES autogen
            DEPENDERS configure
            WORKING_DIRECTORY <BINARY_DIR>)

    ExternalProject_Add_Step(libffi build-impl
            COMMAND $(MAKE)
            DEPENDEES configure
            DEPENDERS build
            WORKING_DIRECTORY <BINARY_DIR>)
endif ()

ExternalProject_Get_Property(libffi SOURCE_DIR BINARY_DIR)

ExternalProject_Add_Step(libffi autogen
        COMMAND env NOCONFIGURE=1 ./autogen.sh
        DEPENDEES patch DEPENDERS configure
        WORKING_DIRECTORY <SOURCE_DIR>)

add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_STRIP} ${STRIP_ARGS} "${CMAKE_BINARY_DIR}/${SONAME}")
if (DIST_PREFIX)
    if (${CMAKE_SYSTEM_NAME} STREQUAL "Windows")
        set(JNI_DIRECTORY_NAME win32)
    else ()
        string(TOLOWER "${CMAKE_SYSTEM_NAME}" JNI_DIRECTORY_NAME)
    endif ()
    add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy_if_different "${CMAKE_BINARY_DIR}/${SONAME}" "${DIST_PREFIX}/${JNI_DIRECTORY_NAME}")
endif ()

include_directories(jni ${BINARY_DIR}/include)
set(LIBFFI_LIBS ${BINARY_DIR}/.libs/libffi.a)

set(SHARE_FLAGS "${SHARE_FLAGS} -MMD -MP -fno-exceptions")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${SHARE_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${SHARE_FLAGS}")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${SHARE_FLAGS} ${LDFLAGS}")

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

target_link_libraries(${PROJECT_NAME} ${LIBFFI_LIBS})
add_dependencies(${PROJECT_NAME} libffi)
add_definitions(-D_JNI_IMPLEMENTATION_)

# force link with C instead of CXX
set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE "C")
set(CMAKE_CXX_IMPLICIT_LINK_LIBRARIES "${CMAKE_C_IMPLICIT_LINK_LIBRARIES}")
set(CMAKE_CXX_IMPLICIT_LINK_DIRECTORIES "${CMAKE_C_IMPLICIT_LINK_DIRECTORIES}")

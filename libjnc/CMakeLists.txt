cmake_minimum_required(VERSION 2.8.12)

if (NOT CMAKE_TOOLCHAIN_FILE)
    set(CMAKE_TOOLCHAIN_FILE default_toolchain.cmake)
endif ()

project(jnc C CXX)

if (APPLE)
    if (NOT CMAKE_LIBTOOL)
        find_program(CMAKE_LIBTOOL NAMES libtool)
    endif ()
    if (CMAKE_LIBTOOL)
        set(CMAKE_LIBTOOL ${CMAKE_LIBTOOL} CACHE PATH "libtool executable")
    endif ()
endif ()

if (NOT SYSTEM_PROCESSOR)
    set(SYSTEM_PROCESSOR ${CMAKE_SYSTEM_PROCESSOR})
endif ()

# when perform a cross build i386 on x86_64
# CMAKE_SIZEOF_VOID_P is 8, but we need 32 here.
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
    set(CMAKE_CXX_STANDARD_REQUIRED ON)
endif ()

file(GLOB SOURCES jni/*.h src/*.h src/*.c src/*.cpp)
add_library(${PROJECT_NAME} SHARED ${SOURCES})

include(ExternalProject)
set(DARWIN_CLANG_WERROR_FIX)

if (CMAKE_CXX_COMPILER_ID MATCHES "Clang")
    include(CheckCXXCompilerFlag)
    check_cxx_compiler_flag("-Wno-stdlibcxx-not-found" HAS_NO_STDLIBCXX_NOT_FOUND)
    if (${HAS_NO_STDLIBCXX_NOT_FOUND})
        set(DARWIN_CLANG_WERROR_FIX "${DARWIN_CLANG_WERROR_FIX} -Wno-stdlibcxx-not-found")
    endif ()
    set(DARWIN_CLANG_WERROR_FIX "${DARWIN_CLANG_WERROR_FIX} -Wno-unused-command-line-argument")
endif ()

set(SHARED_FLAGS "-Wall -Wextra -fPIC -fomit-frame-pointer -fno-ident")
set(LIBFFI_BUILD_CONFIG --enable-structs --enable-raw-api --enable-static --disable-shared --with-pic=yes)
set(LIBFFI_BUILD_ENV
        "CC=${CMAKE_C_COMPILER}"
        "CXX=${CMAKE_CXX_COMPILER}"
        "AR=${CMAKE_AR}"
        "LD=${CMAKE_LINKER}"
        "RANLIB=${CMAKE_RANLIB}")

if (${CMAKE_SYSTEM_NAME} STREQUAL Windows)
    if (${ARCH_BITS} EQUAL 32)
        set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--add-stdcall-alias -Wl,--kill-at")
    endif ()
    set(CMAKE_SHARED_LIBRARY_PREFIX "")
endif ()

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)
    set(CMAKE_SHARED_LIBRARY_SUFFIX .jnilib)
    set(SHARED_FLAGS "${SHARED_FLAGS} -mmacosx-version-min=10.0")
    set(STRIP_ARGS -x)
    set(SONAME ${CMAKE_SHARED_LIBRARY_PREFIX}${PROJECT_NAME}.dylib)
    set(DIST_FILE_NAME ${CMAKE_SHARED_LIBRARY_PREFIX}${PROJECT_NAME}${CMAKE_SHARED_LIBRARY_SUFFIX})
else ()
    set(SHARED_FLAGS "${SHARED_FLAGS} -m${ARCH_BITS}")
    set(STRIP_ARGS -s)
    set(SONAME ${CMAKE_SHARED_LIBRARY_PREFIX}${PROJECT_NAME}${CMAKE_SHARED_LIBRARY_SUFFIX})
    set(DIST_FILE_NAME ${CMAKE_SHARED_LIBRARY_PREFIX}${PROJECT_NAME}-${SYSTEM_PROCESSOR}${CMAKE_SHARED_LIBRARY_SUFFIX})
endif ()

set(TARGET_FILE_NAME ${CMAKE_SHARED_LIBRARY_PREFIX}${PROJECT_NAME}${CMAKE_SHARED_LIBRARY_SUFFIX})

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -install_name ${SONAME}")
    # INSTALL_NAME_DIR for cmake [2.8.12, 3.0)
    set_target_properties(${PROJECT_NAME} PROPERTIES MACOSX_RPATH "")
else ()
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-soname,${SONAME}")
endif ()

string(TOUPPER "${CMAKE_BUILD_TYPE}" BUILD_TYPE_UPPER)

set(LIBFFI_BUILD_CFLAGS "${CMAKE_C_FLAGS} ${SHARED_FLAGS} ${CMAKE_C_FLAGS_${BUILD_TYPE_UPPER}}")
set(LIBFFI_BUILD_CXXFLAGS "${CMAKE_CXX_FLAGS} ${SHARED_FLAGS} ${CMAKE_CXX_FLAGS_${BUILD_TYPE_UPPER}}")
set(LIBFFI_BUILD_LDFLAGS "${CMAKE_STATIC_LINKER_FLAGS} ${SHARED_FLAGS} ${CMAKE_STATIC_LINKER_FLAGS_${BUILD_TYPE_UPPER}}")

# These flags are not applied to libffi build flags.
# With `Werror` libffi get building error
# The other options are managed by libffi building script
set(SHARED_FLAGS "${SHARED_FLAGS} -Werror -MMD -MP -fno-exceptions")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${SHARED_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${SHARED_FLAGS} ${DARWIN_CLANG_WERROR_FIX}")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${SHARED_FLAGS} ${DARWIN_CLANG_WERROR_FIX}")

if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -static -static-libgcc")
endif ()

ExternalProject_Add(libffi
        GIT_REPOSITORY https://github.com/libffi/libffi.git GIT_TAG 80d07104c33045ea34a4d5185600495dc7461a12
        CONFIGURE_COMMAND true
        BUILD_COMMAND true
        INSTALL_COMMAND true
        WORKING_DIRECTORY <BINARY_DIR>)

ExternalProject_Add_Step(libffi autogen
        COMMAND env NOCONFIGURE=1 ./autogen.sh
        DEPENDEES gitinfo.txt patch DEPENDERS configure
        WORKING_DIRECTORY <SOURCE_DIR>)

if (${CMAKE_SYSTEM_NAME} STREQUAL Darwin)

    set(LIBFFI_STATICS)
    set(ARCH_FLAGS)
    set(LIBFFI_TARGET <BINARY_DIR>/.libs/libffi.a)

    foreach (arch i386 x86_64)
        ExternalProject_Add_Step(libffi configure-${arch}
                COMMAND ${CMAKE_COMMAND} -E make_directory <BINARY_DIR>-${arch}
                COMMAND ${CMAKE_COMMAND} -E chdir <BINARY_DIR>-${arch} env
                ${LIBFFI_BUILD_ENV}
                "CFLAGS=${LIBFFI_BUILD_CFLAGS} -arch ${arch}"
                "CXXFLAGS=${LIBFFI_BUILD_CXXFLAGS} -arch ${arch}"
                "LDFLAGS=${LIBFFI_BUILD_LDFLAGS} -arch ${arch}"
                # use relative path, let autoconf determine style of the path
                ../libffi/configure ${LIBFFI_BUILD_CONFIG}
                -host ${arch}-apple-darwin "--prefix=<INSTALL_DIR>"
                DEPENDEES autogen
                DEPENDERS configure)
        ExternalProject_Add_Step(libffi build-${arch}
                COMMAND $(MAKE)
                DEPENDEES configure-${arch}
                DEPENDERS build
                WORKING_DIRECTORY <BINARY_DIR>-${arch})
        set(LIBFFI_STATICS ${LIBFFI_STATICS} <BINARY_DIR>-${arch}/.libs/libffi.a)
        set(ARCH_FLAGS "${ARCH_FLAGS} -arch ${arch}")
    endforeach ()

    ExternalProject_Add_Step(libffi include-dir
            COMMAND ${CMAKE_COMMAND} -E make_directory <BINARY_DIR>/include)

    foreach (name ffi.h ffitarget.h)
        ExternalProject_Add_Step(libffi generate-${name}
                COMMAND ${CMAKE_COMMAND} -E echo "#if defined(__x86_64__)" > "<BINARY_DIR>/include/${name}"
                COMMAND ${CMAKE_COMMAND} -E echo "#include \"../../libffi-build-x86_64/include/${name}\"" >> "<BINARY_DIR>/include/${name}"
                COMMAND ${CMAKE_COMMAND} -E echo "#elif defined(__i386__)" >> "<BINARY_DIR>/include/${name}"
                COMMAND ${CMAKE_COMMAND} -E echo "#include \"../../libffi-build-i386/include/${name}\"" >> "<BINARY_DIR>/include/${name}"
                COMMAND ${CMAKE_COMMAND} -E echo "#endif" >> "<BINARY_DIR>/include/${name}"
                DEPENDEES include-dir
                DEPENDERS install)
    endforeach ()

    ExternalProject_Add_Step(libffi merge-static
            COMMAND ${CMAKE_COMMAND} -E make_directory <BINARY_DIR>/.libs
            COMMAND ${CMAKE_LIBTOOL} -static -o ${LIBFFI_TARGET} ${LIBFFI_STATICS}
            DEPENDEES build DEPENDERS install)

    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${ARCH_FLAGS}")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${ARCH_FLAGS}")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${ARCH_FLAGS}")
else ()
    set(LIBFFI_HOST_CONFIG)
    if (CMAKE_C_COMPILER_TARGET)
        set(LIBFFI_HOST_CONFIG "--host" "${CMAKE_C_COMPILER_TARGET}")
    endif ()
    ExternalProject_Add_Step(libffi configure-impl
            COMMAND env ${LIBFFI_BUILD_ENV}
            "CFLAGS=${LIBFFI_BUILD_CFLAGS}"
            "CXXFLAGS=${LIBFFI_BUILD_CXXFLAGS}"
            "LDFLAGS=${LIBFFI_BUILD_LDFLAGS}"
            # use relative path, let autoconf determine style of the path
            ../libffi/configure ${LIBFFI_BUILD_CONFIG}
            ${LIBFFI_HOST_CONFIG} "--prefix=<INSTALL_DIR>"
            DEPENDEES autogen
            DEPENDERS configure
            WORKING_DIRECTORY <BINARY_DIR>)

    ExternalProject_Add_Step(libffi build-impl
            COMMAND $(MAKE)
            DEPENDEES configure
            DEPENDERS build
            WORKING_DIRECTORY <BINARY_DIR>)
endif ()

add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_STRIP} ${STRIP_ARGS} "${CMAKE_BINARY_DIR}/${TARGET_FILE_NAME}")
if (DIST_DIR)
    # chanage backslash to slash on windows
    get_filename_component(DIST_DIR "${DIST_DIR}" ABSOLUTE)
    #ensure property DIST_DIR present when rebuild
    set(DIST_DIR "${DIST_DIR}" CACHE PATH "dist directory")
    string(TOLOWER "${CMAKE_SYSTEM_NAME}" JNI_DIRECTORY_NAME)

    set(JNI_DIRECTORY "${DIST_DIR}/${JNI_DIRECTORY_NAME}")
    add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E make_directory "${JNI_DIRECTORY}"
            COMMAND ${CMAKE_COMMAND} -E echo "copy ${TARGET_FILE_NAME} to ${JNI_DIRECTORY}/${DIST_FILE_NAME}"
            COMMAND ${CMAKE_COMMAND} -E copy_if_different "${CMAKE_BINARY_DIR}/${TARGET_FILE_NAME}" "${JNI_DIRECTORY}/${DIST_FILE_NAME}")
endif ()

ExternalProject_Get_Property(libffi BINARY_DIR)
set(LIBFFI_BINARY_DIR "${BINARY_DIR}")
include_directories(jni "${LIBFFI_BINARY_DIR}/include")
set(LIBFFI_LIBS "${LIBFFI_BINARY_DIR}/.libs/libffi.a")

target_link_libraries(${PROJECT_NAME} ${LIBFFI_LIBS})
add_dependencies(${PROJECT_NAME} libffi)
add_definitions(-DSTRICT)

# force link with C instead of CXX
set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE "C")

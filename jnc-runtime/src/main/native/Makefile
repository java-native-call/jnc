#
#  There exist several targets which are by default empty and which can be
#  used for execution of your targets. These targets are usually executed
#  before and after some main targets. They are:
#
#     .build-pre:              called before 'build' target
#     .build-post:             called after 'build' target
#     .clean-pre:              called before 'clean' target
#     .clean-post:             called after 'clean' target
#     .clobber-pre:            called before 'clobber' target
#     .clobber-post:           called after 'clobber' target
#     .all-pre:                called before 'all' target
#     .all-post:               called after 'all' target
#     .help-pre:               called before 'help' target
#     .help-post:              called after 'help' target
#
#  Targets beginning with '.' are not intended to be called on their own.
#
#  Main targets can be executed directly, and they are:
#
#     build                    build a specific configuration
#     clean                    remove built files from a configuration
#     clobber                  remove all built files
#     all                      build all configurations
#     help                     print help mesage
#
#  Targets .build-impl, .clean-impl, .clobber-impl, .all-impl, and
#  .help-impl are implemented in nbproject/makefile-impl.mk.
#
#  Available make variables:
#
#     CND_BASEDIR                base directory for relative paths
#     CND_DISTDIR                default top distribution directory (build artifacts)
#     CND_BUILDDIR               default top build directory (object files, ...)
#     CONF                       name of current configuration
#     CND_PLATFORM_${CONF}       platform name (current configuration)
#     CND_ARTIFACT_DIR_${CONF}   directory of build artifact (current configuration)
#     CND_ARTIFACT_NAME_${CONF}  name of build artifact (current configuration)
#     CND_ARTIFACT_PATH_${CONF}  path to build artifact (current configuration)
#     CND_PACKAGE_DIR_${CONF}    directory of package (current configuration)
#     CND_PACKAGE_NAME_${CONF}   name of package (current configuration)
#     CND_PACKAGE_PATH_${CONF}   path to package (current configuration)
#
# NOCDDL


# Environment
MKDIR = mkdir
CP = cp
CCADMIN = CCadmin


# build
build: .build-post

.build-pre:
# Add your pre 'build' code here...

.build-post: .build-impl
# Add your post 'build' code here...


# clean
clean: .clean-post

.clean-pre:
# Add your pre 'clean' code here...

.clean-post: .clean-impl
# Add your post 'clean' code here...


# clobber
clobber: .clobber-post

.clobber-pre:
# Add your pre 'clobber' code here...

.clobber-post: .clobber-impl
# Add your post 'clobber' code here...


# all
all: .all-post

.all-pre:
# Add your pre 'all' code here...

.all-post: .all-impl
# Add your post 'all' code here...


# build tests
build-tests: .build-tests-post

.build-tests-pre:
# Add your pre 'build-tests' code here...

.build-tests-post: .build-tests-impl
# Add your post 'build-tests' code here...


# run tests
test: .test-post

.test-pre: build-tests
# Add your pre 'test' code here...

.test-post: .test-impl
# Add your post 'test' code here...


# help
help: .help-post

.help-pre:
# Add your pre 'help' code here...

.help-post: .help-impl
# Add your post 'help' code here...

AR = ar
CC = gcc
CXX = g++
LD = ld
RANLIB = ranlib
STRIP = strip

OS = $(strip $(shell uname -s | tr '[:upper:]' '[:lower:]' | sed \
    -e 's/^mingw.*$$/win32/g' \
    -e 's/^cygwin.*$$/win32/g'))
ARCH ?= $(shell uname -m | sed \
    -e 's/^i[3-6]86$$/i386/g' \
    -e 's/^x86$$/i386/g' \
    -e 's/^pentium$$/i386/g' \
    -e 's/^x\(86[_-]\)\{0,1\}64$$/x86_64/g' \
    -e 's/^amd64$$/x86_64/g' \
    -e 's/^em64t$$/x86_64/g')
MODEL = $(if $(filter %,$(findstring 64,$(ARCH)) $(filter $(ARCH), sparcv9 s390x)),64,32)

PLATFORM ?= $(OS)-$(ARCH)
ARCH_SUFFIX = -$(ARCH)
LIB_PREFIX = lib
LIBNAME = jnc
JNI_EXT = so
WARNING_FLAGS = -Wall -Wextra -Werror
CFLAGS = $(WARNING_FLAGS) -D_JNI_IMPLEMENTATION_
ifneq (S, ${OBJ_TYPE})
    override OBJ_TYPE = o
else
    CFLAGS += -S
endif
LDFLAGS = $(WARNING_FLAGS) -shared
LIBFFI_BUILD_CFLAGS = -Wall -Wextra
LIBFFI_BUILD_LDFLAGS = -Wall -Wextra
PWD = $(shell pwd)
SOURCE_DIR ?= $(PWD)
BUILD_DIR ?= $(PWD)/../../../target/build
JAVAH_INCLUDE_DIR ?= $(PWD)/../../../target/header
# fix for windows, assume not back slash in the filename
FixPath = $(shell echo '$(1)' | sed 's/\\/\//g')
OBJECT_DIR = $(call FixPath,$(BUILD_DIR)/$(PLATFORM))
DIST_PREFIX ?= $(call FixPath,$(BUILD_DIR))
DIST_DIR = $(DIST_PREFIX)/$(OS)

SRCS = $(wildcard $(SOURCE_DIR)/*.c $(SOURCE_DIR)/*.cpp)
OBJS = $(patsubst %,$(OBJECT_DIR)/%.$(OBJ_TYPE),$(basename $(notdir $(SRCS))))
LIBJNC = $(call FixPath,$(DIST_DIR)/$(LIB_PREFIX)$(LIBNAME)$(ARCH_SUFFIX).$(JNI_EXT))
SONAME = $(LIBNAME).$(JNI_EXT)

#LINK.c = $(if $(filter %.cpp,$(SRCS)),$(CXX),$(CC))
LINK.c = $(CC)
HOST =
SHARE_FLAGS = -Os -fPIC -fomit-frame-pointer -fno-ident
CFLAGS += $(SHARE_FLAGS)
LDFLAGS += $(SHARE_FLAGS)
LIBFFI_BUILD_CFLAGS += $(SHARE_FLAGS)
LIBFFI_BUILD_LDFLAGS += $(SHARE_FLAGS)

ifeq (win32, $(OS))
    ifeq (64, $(MODEL))
        HOST = x86_64-w64-mingw32
    else
        HOST = i686-w64-mingw32
        LDFLAGS += -Wl,--add-stdcall-alias -Wl,--kill-at
    endif
    AR = $(CC)-ar
    RANLIB = $(CC)-ranlib
    LIB_PREFIX =
    JNI_EXT = dll
else ifeq (darwin, $(OS))
    PLATFORM = darwin
    ARCH_SUFFIX =
    JNI_EXT = jnilib
    ARCHES =
    ifneq (, $(findstring $(ARCH), i386 x86_64))
        ARCHES += i386 x86_64
    else
        ARCHES += $(ARCH)
    endif
    #SDK_HOME = $(shell xcode-select -print-path)
    #CFLAGS += -isysroot "$(SDK_HOME)" -I"$(SDK_HOME)/usr/include"
    SHARE_FLAGS += -mmacosx-version-min=10.1
    LDFLAGS += -dynamiclib -install_name $(SONAME)
    #LIBFFI_LDFLAGS += -Wl,-syslibroot,$(SDK_HOME)
    ARCHS_FLAGS = $(foreach arch, $(ARCHES), -arch $(arch))
else ifneq (, $(findstring sunos, $(OS)))
    OS = solaris
endif

ifneq (, $(HOST))
    CC = $(HOST)-gcc
    CXX = $(HOST)-g++
    LIBFFI_HOST = $(HOST)
endif

ifeq (darwin, $(OS))
    CFLAGS += $(ARCHS_FLAGS)
    LDFLAGS += $(ARCHS_FLAGS) -lc -lm
    STRIP += -x
else
    SHARE_FLAGS += -m$(MODEL)
    STRIP += -s
    LDFLAGS += -static -static-libgcc
    LIBFFI_BUILD_LDFLAGS += -static -static-libgcc
endif

ifeq (1, $(USE_SYSTEM_LIBFFI))
    LIBFFI_LIBS ?= $(shell pkg-config --libs libffi)
    LIBFFI_CFLAGS ?= $(shell pkg-config --cflags libffi)
else
    LIBFFI_SRC_DIR = $(SOURCE_DIR)/libffi
    LIBFFI_BUILD_DIR = $(call FixPath,$(BUILD_DIR)/libffi-$(PLATFORM))
    LIBFFI_BUILD_OUTPUT = $(LIBFFI_BUILD_DIR)/.libs/libffi.a
    LIBFFI_LIBS = $(LIBFFI_BUILD_OUTPUT)
    LIBFFI_CFLAGS = -I"$(LIBFFI_BUILD_DIR)/include"
    LIBFFI_AUTOGEN = $(LIBFFI_SRC_DIR)/autogen.sh
    LIBFFI_CONFIGURE = $(LIBFFI_SRC_DIR)/configure
    LIBFFI_MAKE_CONFIG = --enable-static --disable-shared --with-pic=yes
    ifneq (, $(LIBFFI_HOST))
        LIBFFI_MAKE_CONFIG += --host $(LIBFFI_HOST)
    endif
endif

ifeq (jre, $(notdir $(JAVA_HOME)))
    JDK_HOME=$(JAVA_HOME)/..
else
    JDK_HOME=$(JAVA_HOME)
endif

INCLUDES = -I"$(JDK_HOME)/include" -I"$(JDK_HOME)/include/$(OS)" -I"$(JAVAH_INCLUDE_DIR)" $(LIBFFI_CFLAGS)

# NetBeans
CND_BUILDDIR = $(BUILD_DIR)
CND_PLATFORM = $(PLATFORM)
CND_DLIB_EXT = $(JNI_EXT)
COMPILER.c = $(CC)
#CND_DISTDIR =
#CONF = Release
OBJECTDIR = $(OBJECT_DIR)
LDLIBSOPTIONS=

.clean-impl:
	${RM} -r "$(OBJECT_DIR)"
	${RM} -r "$(DIST_DIR)"

.build-impl: $(LIBJNC)

$(LIBJNC): $(OBJS) $(LIBFFI_LIBS)
	${MKDIR} -p "$(@D)"
	$(LINK.c) -o "$@" $(LDFLAGS) $(patsubst %,"%",$^)
	$(STRIP) "$@"

$(LIBFFI_AUTOGEN):
	${MKDIR} -p "$(@D)"
	@gitroot=`git rev-parse --show-toplevel` && \
	    cd "$(@D)" && libffi=`git rev-parse --show-prefix` && \
	    cd "$${gitroot}" && git submodule update --init "$${libffi}"

$(LIBFFI_CONFIGURE): $(LIBFFI_AUTOGEN)
	@cd "$(@D)" && ./$(<F)

ifeq (darwin, $(OS))

$(LIBFFI_BUILD_DIR)-%/Makefile: $(LIBFFI_CONFIGURE)
	${MKDIR} -p "$(@D)"
	@cd "$(@D)" && env CC="$(CC)" CXX="$(CXX)" LD="$(LD)" AR="$(AR)" \
	    RANLIB="$(RANLIB)" CFLAGS="$(LIBFFI_BUILD_CFLAGS) -arch $*" \
	    LDFLAGS="$(LIBFFI_BUILD_LDFLAGS) -arch $*" \
	    CXXFLAGS="$(LIBFFI_BUILD_CFLAGS)" $< $(LIBFFI_MAKE_CONFIG) \
	    --host "$*-apple-darwin"

$(LIBFFI_BUILD_OUTPUT): $(patsubst %,$(LIBFFI_BUILD_DIR)-%/.libs/libffi.a, $(ARCHES))
	${MKDIR} -p "$(@D)"
	${CP} -R "$(LIBFFI_BUILD_DIR)-$(ARCH)/include" "$(LIBFFI_BUILD_DIR)/include"
	/usr/bin/libtool -static -o "$@" $?

else

$(LIBFFI_BUILD_DIR)/Makefile: $(LIBFFI_CONFIGURE)
	${MKDIR} -p "$(@D)"
	@cd "$(@D)" && env CC="$(CC)" CXX="$(CXX)" LD="$(LD)" AR="$(AR)" \
	    RANLIB="$(RANLIB)" CFLAGS="$(LIBFFI_BUILD_CFLAGS)" \
	    LDFLAGS="$(LIBFFI_BUILD_LDFLAGS)" CXXFLAGS="$(LIBFFI_BUILD_CFLAGS)" \
	    $< $(LIBFFI_MAKE_CONFIG)

endif

%/.libs/libffi.a: %/Makefile
	$(MAKE) -C "$(<D)"

$(OBJECT_DIR)/%.$(OBJ_TYPE) : $(SOURCE_DIR)/%.c $(LIBFFI_LIBS)
	${MKDIR} -p "$(@D)"
	${RM} "$@.d"
	$(CC) $(CFLAGS) $(INCLUDES) -MMD -MP -MF "$@.d" -o "$@" -c "$<"

$(OBJECT_DIR)/%.$(OBJ_TYPE) : $(SOURCE_DIR)/%.cpp $(LIBFFI_LIBS)
	${MKDIR} -p "$(@D)"
	${RM} "$@.d"
	$(CXX) $(CFLAGS) $(INCLUDES) -MMD -MP -MF "$@.d" -o "$@" -c "$<"

ifneq (, $(MAKE_VERSION))
    DEPFILES = $(wildcard $(addsuffix .d, $(OBJS)))
    ifneq (, $(DEPFILES))
        include $(DEPFILES)
    endif
endif

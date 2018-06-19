#include "jnc.h"
#include <float.h>
#include <math.h>
#include <time.h>
#include <wctype.h>

#if (!defined(_MSC_VER) && defined(_WIN32)) || \
    defined(__unix__) || defined(__MACH__)
#include <pthread.h>
#include <sys/fcntl.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/unistd.h>
#endif

#ifdef _WIN32
#include <ws2tcpip.h>
#include <windows.h>
#else /* _WIN32 */
#include <netinet/in.h>
#include <sys/resource.h>
#include <sys/signal.h>
#include <sys/socket.h>
#endif /* _WIN32 */

#ifdef __cplusplus

#include <limits>

namespace jnc_type_traits {

    template <class _Tp, _Tp v> struct integral_constant {
        static const _Tp value = v;
    };

#define TRUE_TYPE(x) x : integral_constant<bool, true> {}
#define FALSE_TYPE(x) x : integral_constant<bool, false> {}

    FALSE_TYPE(template<typename T> struct is_pointer);
    TRUE_TYPE(template<typename T> struct is_pointer<T*>);

    FALSE_TYPE(template<typename T> struct is_floating_point);
    TRUE_TYPE(template<> struct is_floating_point<float>);
    TRUE_TYPE(template<> struct is_floating_point<double>);
    TRUE_TYPE(template<> struct is_floating_point<long double>);

#if (__GNUC__ > 4) || (__GNUC__ == 4 && __GNUC_MINOR__ >= 3)
#define JNC_HAS_FETURE_IS_ENUM 1
#elif defined(__is_enum)
#define JNC_HAS_FETURE_IS_ENUM 1
#elif defined(__has_feature)
#if __has_feature(is_enum)
#define JNC_HAS_FETURE_IS_ENUM 1
#else
#define JNC_HAS_FETURE_IS_ENUM 0
#endif
#else
#define JNC_HAS_FETURE_IS_ENUM 0
#endif
#if JNC_HAS_FETURE_IS_ENUM

    template<typename T> struct is_enum : integral_constant<bool, __is_enum(T)> {
    };

#else
    FALSE_TYPE(template<typename T> struct is_enum);
#endif
#undef FALSE_TYPE
#undef TRUE_TYPE

    template<typename T,
    bool = is_enum<T>::value,
    bool = is_pointer<T>::value,
    bool = ::std::numeric_limits<T>::is_integer,
    bool = is_floating_point<T>::value>
    struct get_ffi_type : integral_constant<int, -1 > {
    };

    template<typename T> struct get_ffi_type<T, false, true, false, false> : integral_constant<FFI_TYPE, JNC_TYPE(POINTER)> {
    };

    /* we may got warning for unsigned types, such as size_t,
     * here we use 1 to compare, it's also available */
    template<typename T> struct is_signed : integral_constant<bool, T(-1) < T(1)> { };

    template<typename T> struct size_helper {
        static const size_t size = sizeof (T);
        static const size_t align = alignof (T);
        static const bool signed_ = is_signed<T>::value;
    };

    template<size_t, size_t, bool>
    struct get_ffi_type_by_size : integral_constant<int, -1 > {
    };

#define DEF(type, value) \
    template<> struct get_ffi_type_by_size< \
    size_helper<type>::size, \
    size_helper<type>::align, \
    size_helper<type>::signed_ \
    > : \
    integral_constant<FFI_TYPE, JNC_TYPE(value)> {};

    DEF(uint8_t, UINT8)
    DEF(int8_t, SINT8)
    DEF(uint16_t, UINT16)
    DEF(int16_t, SINT16)
    DEF(uint32_t, UINT32)
    DEF(int32_t, SINT32)
    DEF(uint64_t, UINT64)
    DEF(int64_t, SINT64)
#undef DEF

    template<typename T> struct get_ffi_type_integral : get_ffi_type_by_size<
    size_helper<T>::size,
    size_helper<T>::align,
    size_helper<T>::signed_> {
    };

    /* integer */
    template<typename T> struct get_ffi_type<T, false, false, true, false> : get_ffi_type_integral<T> {
    };

    /* enum */
    template<typename T> struct get_ffi_type<T, true, false, false, false> : get_ffi_type_integral<T> {
    };

}

#define getFFITypeValue(type) (jnc_type_traits::get_ffi_type<type>::value)

#else /* __cplusplus */

static inline bool isNaN(double x) {
    return x != x;
}

#ifndef NAN
#define NAN (0.0 / 0.0)
#endif

/* we may got warning for unsigned types, such as size_t,
 * here we use 1 to compare, it's also available */
#define isSigned(type) ((type)-1 < 1)
/* we can't detect pointer type with C code */
#define isPointer(type) 0
#define isInteger(type) !isNaN((type) NAN)

#define F(type, name, jnc_type_name) \
(sizeof(type) == sizeof(name) && alignof(type) == alignof(name) && \
isSigned(type) == isSigned(name)) ? JNC_TYPE(jnc_type_name) :
#define getFFITypeValue(type) \
isPointer(type) ? JNC_TYPE(POINTER) : \
!isInteger(type) ? -1 : \
F(type, uint8_t, UINT8) \
F(type, int8_t, SINT8) \
F(type, uint16_t, UINT16) \
F(type, int16_t, SINT16) \
F(type, uint32_t, UINT32) \
F(type, int32_t, SINT32) \
F(type, uint64_t, UINT64) \
F(type, int64_t, SINT64) \
-1

#endif /* __cplusplus */

#define MAX_N 128
#define INDEX_MASK 127
static const char* typeName[MAX_N]; /* 1024B/512B on 64/32 bit machine */
static uint8_t typeValue[MAX_N]; /* 128B */

#define COMPILE_ERROR_ON_ZERO(x) (sizeof(char[1 - 2 * !(x)]) - 1)
#define ASSERT_NOT_M1(x) (x + COMPILE_ERROR_ON_ZERO(~(x)))
#define DEFINE(name) {#name, ASSERT_NOT_M1(getFFITypeValue(name))},
template<class T, size_t N> char (&array_size_helper(T (&array)[N]))[N];
#define array_size(array)  (sizeof(array_size_helper(array)))

static int32_t hashString(const char *name) {
    int32_t ret = 0;
    uint8_t ch;
    for (; (ch = *name) != 0; ++name) {
        ret = 97 * ret ^ ch;
    }
    return ret ^ ((uint32_t) ret >> 16);
}

static void add(const char * name, int value) {
    int index = hashString(name) & INDEX_MASK, origin = index;
    do {
        if (likely(NULL == typeName[index])) {
            typeName[index] = name;
            typeValue[index] = value;
            return;
        }
    } while (likely((index = (index + 1) & INDEX_MASK) != origin));
}

/* initialize not thread safe */
static void init() {
    typedef void *pointer;

    static const struct {
        const char * name;
        int8_t v;
    } tuples[] = {
        /* on some platform sizeof(int) = 2 */
        DEFINE(int)
        /* maybe sizeof(long) = 4 or 8 */
        DEFINE(long)
        DEFINE(clock_t)
        /* enum on darwin */
        DEFINE(clockid_t)
        DEFINE(dev_t)
        /* use int instead */
        // DEFINE(errno_t)
        DEFINE(ino_t)
        // DEFINE(ino64_t)
        DEFINE(int16_t)
        DEFINE(int32_t)
        DEFINE(int64_t)
        DEFINE(int8_t)
        // DEFINE(intmax_t)
        DEFINE(intptr_t)
        DEFINE(mode_t)
        DEFINE(off_t)
        DEFINE(pid_t)
        DEFINE(pointer) // should not be used, just for test
        DEFINE(ptrdiff_t)
        // DEFINE(rsize_t)
        DEFINE(size_t)
        DEFINE(socklen_t)
        DEFINE(ssize_t)
        DEFINE(time_t)
        DEFINE(uint16_t)
        DEFINE(uint32_t)
        DEFINE(uint64_t)
        DEFINE(uint8_t)
        // DEFINE(uintmax_t)
        DEFINE(uintptr_t)
        DEFINE(useconds_t)
        DEFINE(wchar_t)
        /*
         * linux: typedef int32_t *wctrans_t;
         * aix: typedef wint_t (*wctrans_t)();
         * solaris typedef unsigned int wctrans_t;
         * mingw typedef wchar_t wctrans_t;
         */
        DEFINE(wctrans_t)
        /* pointer type on OpenBSD */
        DEFINE(wctype_t)
        DEFINE(wint_t)
#ifndef _WIN32
        DEFINE(blkcnt_t)
        DEFINE(blksize_t)
        DEFINE(fsblkcnt_t)
        DEFINE(fsfilcnt_t)
        DEFINE(gid_t)
        DEFINE(id_t)
        DEFINE(in_addr_t)
        DEFINE(in_port_t)
        DEFINE(key_t)
        /* not an integer type */
        // DEFINE(mbstate_t)
        DEFINE(nlink_t)
        /* not an integer type */
        // DEFINE(pthread_attr_t)
        DEFINE(rlim_t)
        DEFINE(sa_family_t)
        /* maybe not an integer type */
        /* https://www.gnu.org/software/libc/manual/html_node/Signal-Sets.html */
        // DEFINE(sigset_t)
        DEFINE(suseconds_t)
        DEFINE(uid_t)
#ifdef BSD
        /* BSD (DragonFly BSD, FreeBSD, OpenBSD, NetBSD). ----------- */
        DEFINE(register_t)
        DEFINE(segsz_t)
#endif /* BSD */
#ifdef __MACH__
        DEFINE(ct_rune_t)
        DEFINE(rune_t)
        DEFINE(sae_associd_t)
        DEFINE(sae_connid_t)
        // DEFINE(swblk_t)
        DEFINE(syscall_arg_t)
        DEFINE(user_addr_t)
        DEFINE(user_long_t)
        DEFINE(user_off_t)
        DEFINE(user_size_t)
        DEFINE(user_ssize_t)
        DEFINE(user_time_t)
        DEFINE(user_ulong_t)
#endif /* __MACH__ */
#endif /* _WIN32 */
        // DEFINE(pthread_barrier_t)
        // DEFINE(timer_t)
        // DEFINE(trace_attr_t)
    };

    size_t i = 0;
    for (; i < array_size(tuples); ++i) {
        add(tuples[i].name, tuples[i].v);
    }
}

static int find(const char * name) {
    int index = hashString(name) & INDEX_MASK, origin = index;
    do {
        if (unlikely(NULL == typeName[index])) break;
        if (likely(!strcmp(typeName[index], name))) return typeValue[index];
    } while (likely((index = (index + 1) & INDEX_MASK) != origin));
    return -1;
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    findAlias
 * Signature: (Ljava/lang/String;)I
 */
EXTERNC JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_findAlias
(JNIEnv * env, jobject self, jstring string) {
    checkNullPointer(env, string, 0);
    static volatile bool inited = false;
    if (unlikely(!inited)) {
        /* get something to lock. */
        if (likely(CALLJNI(env, MonitorEnter, self) == JNI_OK)) {
            if (likely(!inited)) {
                inited = true;
                init();
            }
            CALLJNI(env, MonitorExit, self);
        } else {
            /* lock failed, throw IllegalStateException if no exception in jni env */
            if (!CALLJNI(env, ExceptionCheck)) throwByName(env, IllegalState, NULL);
            return -1;
        }
#if DEBUG
        const char* type2name[] = {
            "void",
            "int?",
            "float",
            "double",
            "long double",
            "uint8",
            "int8",
            "uint16",
            "int16",
            "uint32",
            "int32",
            "uint64",
            "int64",
            "structure",
            "pointer",
            "complex"
        };
        for (int i = 0; i < MAX_N; ++i) {
            if (typeName[i]) {
                printf("%02x %08x %-16s%s\n", i, hashString(typeName[i]),
                        typeName[i], type2name[typeValue[i]]);
            }
        }
#endif
    }
#define MAX_NAME 15
    jsize len = CALLJNI(env, GetStringUTFLength, string);
    jint res;
    if (likely(len <= MAX_NAME)) {
        char name[MAX_NAME + 1];
        CALLJNI(env, GetStringUTFRegion, string, 0, len, name);
        name[len] = 0;
        res = find(name);
    } else {
        res = -1;
    }
    if (unlikely(res == -1)) {
        throwByName(env, UnsupportedOperation, NULL);
    }
    return res;
}

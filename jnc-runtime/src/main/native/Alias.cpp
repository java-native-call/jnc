#include "jnc.h"
#include <cfloat>
#include <cmath>
#include <ctime>
#include <cwctype>

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

#include <limits>

namespace jnc_type_traits {

    template<class _Tp, _Tp v> struct integral_constant {

        enum : _Tp {
            value = v
        };
    };

    template<bool v> using bool_constant = integral_constant<bool, v>;

    template<class> struct is_pointer : bool_constant<false> {
    };

    /* just ignore const and volatile */
    template<class T> struct is_pointer<T *> : bool_constant<true> {
    };

#if (__GNUC__ > 4) || (__GNUC__ == 4 && __GNUC_MINOR__ >= 3)
#define JNC_IS_ENUM(T) __is_enum(T)
#elif defined(__clang__) && defined(__has_feature)
#if __has_feature(is_enum)
#define JNC_IS_ENUM(T) __is_enum(T)
#else
#define JNC_IS_ENUM(T) false
#endif
#else
#define JNC_IS_ENUM(T) false
#endif

    /*
     * must implement this, for std::numeric_limits::is_signed is false on enum
     * use -1<1 rather -1<0 to avoid compiler warning.
     */
    template<class T> struct is_signed : bool_constant<T(-1) < T(1)> { };

    template<size_t, size_t, bool>
    struct match_ffi_type;

#define DEF(T, v) template<> struct match_ffi_type<sizeof(T), alignof(T), is_signed<T>::value> : integral_constant<int, JNC_TYPE(v)> {}

    DEF(uint8_t, UINT8);
    DEF(int8_t, SINT8);
    DEF(uint16_t, UINT16);
    DEF(int16_t, SINT16);
    DEF(uint32_t, UINT32);
    DEF(int32_t, SINT32);
    DEF(uint64_t, UINT64);
    DEF(int64_t, SINT64);
#undef DEF

    template<class T, bool = JNC_IS_ENUM(T) || std::numeric_limits<T>::is_integer, bool = is_pointer<T>::value>
    struct get_ffi_type;

    template<class T> struct get_ffi_type<T, false, true> : integral_constant<int, JNC_TYPE(POINTER)> {
    };

    /* integer or enum */
    template<class T> struct get_ffi_type<T, true, false> : match_ffi_type<sizeof (T), alignof (T), is_signed<T>::value> {
    };

}

#define MAX_N 128
static const char *typeName[MAX_N]; /* 1024B/512B on 64/32 bit machine */
static uint8_t typeValue[MAX_N]; /* 128B */

#define DEFINE(name) {#name, jnc_type_traits::get_ffi_type<name>::value},

template<class T, size_t N>
static constexpr size_t array_size(T(&)[N]) noexcept {
    return N;
}

static uint32_t hashString(const char *name) noexcept {
    uint32_t ret = 0;
    uint8_t ch;
    for (; (ch = static_cast<uint8_t> (*name)) != 0; ++name) {
        ret = 97U * ret ^ ch;
    }
    return ret ^ ((uint32_t) ret >> 16U);
}

static void add(const char *name, int value) noexcept {
    uint32_t index = hashString(name) % MAX_N, origin = index;
    do {
        if (likely(nullptr == typeName[index])) {
            typeName[index] = name;
            typeValue[index] = static_cast<uint8_t> (value);
            return;
        }
    } while (likely((index = (index + 1U) % MAX_N) != origin));
}

/* initialize not thread safe */
static void init() noexcept {
    typedef void *pointer;

    const struct {
        const char *name;
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

    for (size_t i = 0; i < array_size(tuples); ++i) {
        add(tuples[i].name, tuples[i].v);
    }
}

static int find(const char *name) noexcept {
    uint32_t index = hashString(name) % MAX_N, origin = index;
    do {
        if (unlikely(nullptr == typeName[index])) break;
        if (likely(!strcmp(typeName[index], name))) return typeValue[index];
    } while (likely((index = (index + 1) % MAX_N) != origin));
    return -1;
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    findAlias
 * Signature: (Ljava/lang/String;)I
 */
extern "C" JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_findAlias
(JNIEnv *env, jobject self, jstring string) {
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
            if (!CALLJNI(env, ExceptionCheck)) throwByName(env, IllegalState, nullptr);
            return -1;
        }
#ifndef NDEBUG
        const char *type2name[] = {
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
        throwByName(env, UnsupportedOperation, nullptr);
    }
    return res;
}

#include "jnc.h"
#include <float.h>
#include <math.h>
#include <time.h>
#include <wctype.h>

// see http://nadeausoftware.com/articles/2012/01/c_c_tip_how_use_compiler_predefined_macros_detect_operating_system
#if (!defined(_MSC_VER) && defined(_WIN32)) || \
    defined(__unix) || defined(__unix__) || defined(__MACH__)
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

namespace jnc_type_traits {

    template<class _Tp, _Tp v> struct integral_constant {

        enum : _Tp {
            value = v
        };
    };

    template<bool v> using bool_constant = integral_constant<bool, v>;

    using true_type = bool_constant<true>;
    using false_type = bool_constant<false>;

    template<class> struct is_pointer : false_type {
    };

    /* just ignore const and volatile */
    template<class T> struct is_pointer<T *> : true_type {
    };

#if (__GNUC__ > 4) || (__GNUC__ == 4 && __GNUC_MINOR__ >= 3)

    template<class T> struct is_enum : bool_constant<__is_enum(T) > {
    };

#elif defined(__clang__) && defined(__has_feature)
#if __has_feature(is_enum)

    template<class T> struct is_enum : bool_constant<__is_enum(T)> {
    };
#else

    template<class> struct is_enum : false_type {
    };
#endif
#else

    template<class> struct is_enum : false_type {
    };
#endif

    /*
     * must implement this, for std::numeric_limits::is_signed is false on enum
     * use -1<1 rather -1<0 to avoid compiler warning.
     */
    template<class T> struct is_signed : bool_constant<T(-1) < T(1)> { };

    template<class> struct is_integral : false_type {
    };

#define DEF_INTEGRAL(T) template<> struct is_integral<T> : true_type {}
    DEF_INTEGRAL(bool);
    DEF_INTEGRAL(char);
    DEF_INTEGRAL(signed char);
    DEF_INTEGRAL(unsigned char);
    DEF_INTEGRAL(char16_t);
    DEF_INTEGRAL(char32_t);
    DEF_INTEGRAL(wchar_t);
    DEF_INTEGRAL(short);
    DEF_INTEGRAL(unsigned short);
    DEF_INTEGRAL(int);
    DEF_INTEGRAL(unsigned);
    DEF_INTEGRAL(long);
    DEF_INTEGRAL(unsigned long);
    DEF_INTEGRAL(long long);
    DEF_INTEGRAL(unsigned long long);
#undef DEF_INTEGRAL

}

using namespace jnc_type_traits;

template<size_t, size_t, bool> struct integral_matcher;

#define DEF_MATCHER(T, v) template<> \
struct integral_matcher<sizeof(T), alignof(T), is_signed<T>::value> : \
        integral_constant<int, JNC_TYPE(v)> {}
DEF_MATCHER(uint8_t, UINT8);
DEF_MATCHER(int8_t, SINT8);
DEF_MATCHER(uint16_t, UINT16);
DEF_MATCHER(int16_t, SINT16);
DEF_MATCHER(uint32_t, UINT32);
DEF_MATCHER(int32_t, SINT32);
DEF_MATCHER(uint64_t, UINT64);
DEF_MATCHER(int64_t, SINT64);
#undef DEF_MATCHER

template<class T, bool = is_enum<T>::value || is_integral<T>::value, bool = is_pointer<T>::value>
struct ffi_value;

template<class T> struct ffi_value<T, false, true> : integral_constant<int, JNC_TYPE(POINTER)> {
};

/* integer or enum */
template<class T> struct ffi_value<T, true, false> : integral_matcher<sizeof (T), alignof (T), is_signed<T>::value> {
};

#define DEFINE(type) {#type, ffi_value<type>::value},

template<class T, size_t N>
static constexpr size_t array_size(T(&)[N]) noexcept {
    return N;
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    initAlias
 * Signature: (Ljava/util/Map;)V
 */
extern "C"
JNIEXPORT void JNICALL Java_jnc_foreign_internal_NativeMethods_initAlias
(JNIEnv *env, jobject UNUSED(self), jobject obj) {
    checkNullPointer(env, obj, /*void*/);
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
        // DEFINE(timer_t)
        // DEFINE(trace_attr_t)
    };

    jclass map = env->FindClass("java/util/Map");
    if (unlikely(env->ExceptionCheck())) return;
    jmethodID put = env->GetMethodID(map, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (unlikely(env->ExceptionCheck())) return;
    jclass integer = env->FindClass("java/lang/Integer");
    if (unlikely(env->ExceptionCheck())) return;
    jmethodID valueOf = env->GetStaticMethodID(integer, "valueOf", "(I)Ljava/lang/Integer;");
    if (unlikely(env->ExceptionCheck())) return;

    for (size_t i = 0; i < array_size(tuples); ++i) {
        jvalue args[2];
        args[0].i = tuples[i].v;
        jobject value = env->CallStaticObjectMethodA(integer, valueOf, args);
        if (unlikely(env->ExceptionCheck())) return;
        jobject name = env->NewStringUTF(tuples[i].name);
        if (unlikely(env->ExceptionCheck())) return;
        args[0].l = name;
        args[1].l = value;
        jobject result = env->CallObjectMethodA(obj, put, args);
        if (unlikely(result != NULL)) env->DeleteLocalRef(result);
        if (unlikely(env->ExceptionCheck())) return;
        env->DeleteLocalRef(name);
        if (unlikely(env->ExceptionCheck())) return;
        env->DeleteLocalRef(value);
        if (unlikely(env->ExceptionCheck())) return;
    }

}

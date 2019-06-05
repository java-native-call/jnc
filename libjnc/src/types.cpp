#include "jnc.h"
#include <float.h>
#include <math.h>
#include <time.h>
#include <wctype.h>

// see http://nadeausoftware.com/articles/2012/01/c_c_tip_how_use_compiler_predefined_macros_detect_operating_system
#include <sys/fcntl.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/unistd.h>

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

#if defined(__clang__) && defined(__has_feature)
#define COMPILER_HAS_IS_ENUM __has_feature(is_enum)
#elif defined(__GNUC__)
#define COMPILER_HAS_IS_ENUM ((__GNUC__ > 4) || (__GNUC__ == 4 && __GNUC_MINOR__ >= 3))
#endif // __GNUC__

#if COMPILER_HAS_IS_ENUM

    template<class T> struct is_enum : bool_constant<__is_enum(T)> {
    };

#else

    /** It seems we only need to process type `clockid_t` on darwin,
     * just leave false for other cases. 
     */
    template<class> struct is_enum : false_type {
    };

#endif

    template<class> struct is_integral : false_type {
    };

#define DEFINE_INTEGRAL(T) template<> struct is_integral<T> : true_type {}
    DEFINE_INTEGRAL(bool);
    DEFINE_INTEGRAL(char);
    DEFINE_INTEGRAL(signed char);
    DEFINE_INTEGRAL(unsigned char);
    // DEFINE_INTEGRAL(char8_t);
    DEFINE_INTEGRAL(char16_t);
    DEFINE_INTEGRAL(char32_t);
    DEFINE_INTEGRAL(wchar_t);
    DEFINE_INTEGRAL(short);
    DEFINE_INTEGRAL(unsigned short);
    DEFINE_INTEGRAL(int);
    DEFINE_INTEGRAL(unsigned);
    DEFINE_INTEGRAL(long);
    DEFINE_INTEGRAL(unsigned long);
    DEFINE_INTEGRAL(long long);
    DEFINE_INTEGRAL(unsigned long long);
#undef DEFINE_INTEGRAL

    /*
     * std::is_signed is false on enum, but we should work with will test that
     * use -1<1 rather -1<0 to avoid compiler warning.
     */
    template<class T, bool = is_enum<T>::value || is_integral<T>::value> struct is_signed;

    template<class T> struct is_signed<T, true> : bool_constant<T(-1) < T(1)> { };

    template<class T> struct is_signed<T, false> : false_type {
    };

}

using namespace jnc_type_traits;

template<size_t, size_t, bool> struct integral_matcher;

#define DEFINE_MATCHER(T, v) template<> \
struct integral_matcher<sizeof(T), alignof(T), is_signed<T>::value> : \
        integral_constant<int, JNC_TYPE(v)> {}
DEFINE_MATCHER(uint8_t, UINT8);
DEFINE_MATCHER(int8_t, SINT8);
DEFINE_MATCHER(uint16_t, UINT16);
DEFINE_MATCHER(int16_t, SINT16);
DEFINE_MATCHER(uint32_t, UINT32);
DEFINE_MATCHER(int32_t, SINT32);
DEFINE_MATCHER(uint64_t, UINT64);
DEFINE_MATCHER(int64_t, SINT64);
#undef DEFINE_MATCHER

template<class T, bool = is_enum<T>::value || is_integral<T>::value, bool = is_pointer<T>::value>
struct ffi_value;

template<class T> struct ffi_value<T, false, true> : integral_constant<int, JNC_TYPE(POINTER)> {
};

/* integer or enum */
template<class T> struct ffi_value<T, true, false> : integral_matcher<sizeof (T), alignof (T), is_signed<T>::value> {
};

#define DEFINE(type) {#type, ffi_value<type>::value},

// There is no type errno_t on OpenBSD
// Maybe old MinGW doesn't define this.
// https://en.cppreference.com/w/cpp/language/typedef
// typedef redefinition is allowed in C++
// defined in global scope, compile error if errno_t is not int
typedef int errno_t;

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    initAlias
 * Signature: (Ljava/util/Map;)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_initAlias
(JNIEnv *env, jobject UNUSED(self), jobject obj) {
    checkNullPointer(env, obj, /*void*/);

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
        DEFINE(errno_t)
        // fpos_t is struct on linux
        // DEFINE(fpos_t)
        DEFINE(ino_t)
        // DEFINE(ino64_t)
        DEFINE(int16_t)
        DEFINE(int32_t)
        DEFINE(int64_t)
        DEFINE(int8_t)
        DEFINE(intptr_t)
        DEFINE(mode_t)
        DEFINE(off_t)
        DEFINE(pid_t)
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
        /* BSD (DragonFly BSD, FreeBSD, OpenBSD, NetBSD, Darwin). ----------- */
        DEFINE(register_t)
        DEFINE(segsz_t)
#endif /* BSD */
#ifdef __MACH__
        DEFINE(ct_rune_t)
        DEFINE(rune_t)
        DEFINE(sae_associd_t)
        DEFINE(sae_connid_t)
        DEFINE(swblk_t)
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

    for (auto &&tuple : tuples) {
        jvalue args[2];
        args[0].i = tuple.v;
        jobject value = env->CallStaticObjectMethodA(integer, valueOf, args);
        if (unlikely(env->ExceptionCheck())) return;
        jobject name = env->NewStringUTF(tuple.name);
        if (unlikely(env->ExceptionCheck())) return;
        args[0].l = name;
        args[1].l = value;
        jobject result = env->CallObjectMethodA(obj, put, args);
        if (unlikely(env->ExceptionCheck())) return;
        if (unlikely(result != nullptr)) env->DeleteLocalRef(result);
        env->DeleteLocalRef(name);
        env->DeleteLocalRef(value);
    }

}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getTypes
 * Signature: ()[[J
 */
EXTERNC JNIEXPORT jobjectArray JNICALL
Java_jnc_foreign_internal_NativeMethods_getTypes
(JNIEnv *env, jobject UNUSED(self)) {
#define F(value) &ffi_type_##value
    ffi_type * addrs[] = {
        F(void), F(float), F(double), F(pointer),
        F(uint8), F(sint8), F(uint16), F(sint16),
        F(uint32), F(sint32), F(uint64), F(sint64),
#undef F
    };
    jclass longArray = env->FindClass("[J");
    if (unlikely(env->ExceptionCheck())) return nullptr;
    jobjectArray res = env->NewObjectArray(FFI_TYPE_LAST + 1, longArray, nullptr);
    if (unlikely(env->ExceptionCheck())) return nullptr;

    for (auto &&p : addrs) {
        unsigned short type = p->type;
        jlong info = (uint64_t(p->size) << 32) | (p->alignment << 16) | type;
        jlongArray arr = env->NewLongArray(2);
        if (unlikely(env->ExceptionCheck())) return nullptr;
        jlong region[2] = {p2j(p), info};
        env->SetLongArrayRegion(arr, 0, 2, region);
        if (unlikely(env->ExceptionCheck())) return nullptr;
        env->SetObjectArrayElement(res, type, arr);
        if (unlikely(env->ExceptionCheck())) return nullptr;
        env->DeleteLocalRef(arr);
    }
    return res;
}

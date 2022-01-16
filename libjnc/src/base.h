#pragma once

#include <jni.h>
#include <stdint.h> // NOLINT(modernize-deprecated-headers)
#include <stddef.h> // NOLINT(modernize-deprecated-headers)
#include <string.h> // NOLINT(modernize-deprecated-headers)
#include <stdlib.h> // NOLINT(modernize-deprecated-headers)
#include <wchar.h> // NOLINT(modernize-deprecated-headers)

// JNC_SYMBOL_HIDDEN
#ifdef _WIN32
// attribute hidden is not supported on windows
#define JNC_SYMBOL_HIDDEN
#elif __GNUC__ >= 4
// works for both gcc and clang
#define JNC_SYMBOL_HIDDEN __attribute__ ((visibility ("hidden")))
#else // not defined(__GNUC__) or __GNUC__ < 4
#define JNC_SYMBOL_HIDDEN
#endif // _WIN32, __GNUC__

#ifdef __GNUC__
#define UNUSED(x) UNUSED_ ## x __attribute__((unused))
#elif defined(__LCLINT__)
#define UNUSED(x) /*@unused@*/ UNUSED_ ## x
#else    /* !__GNUC__ && !__LCLINT__ */
#define UNUSED(x) UNUSED_ ## x
#endif   /* !__GNUC__ && !__LCLINT__ */

#ifdef __GNUC__
#define likely(x) __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)
#else
#define likely(x) (x)
#define unlikely(x) (x)
#endif

#define PP_THIRD_ARG(a, b, c, ...) c
#define VA_OPT_SUPPORTED_I(...) PP_THIRD_ARG(__VA_OPT__(,),true,false,)
#define VA_OPT_SUPPORTED VA_OPT_SUPPORTED_I(?)

#ifdef __cplusplus
#define CALLJNI(env, action, ...) env->action(__VA_ARGS__)
#elif VA_OPT_SUPPORTED
#define CALLJNI(env, action, ...) (*env)->action(env __VA_OPT__(,) __VA_ARGS__)
#else
#define CALLJNI(env, action, ...) (*env)->action(env, ##__VA_ARGS__)
#endif

#ifdef __cplusplus
// usually min is not defined in C++
#undef min

#include "jnc_type_traits.h"

namespace jnc {

    template<class Tp>
    constexpr jlong p2j(Tp *x) {
        return jlong(reinterpret_cast<uintptr_t> (x));
    }

    template<class Tp>
    constexpr typename jnc_type_traits::enable_if<jnc_type_traits::is_pointer<Tp>::value, Tp>::type
    j2p_impl(jlong x) {
        return reinterpret_cast<Tp>(uintptr_t(x));
    }

    template<class Tp>
    constexpr const Tp &min(const Tp &a, const Tp &b) {
        return a < b ? a : b;
    }
}

using jnc::p2j;
#define j2p(x, type) jnc::j2p_impl<type>(x)
#define j2c(x, type) jnc::j2p_impl<type*>(x)
#define j2vp(x) j2c(x, void)
#define MIN(x, y) jnc::min(x, y)
#define EXTERNC extern "C"
#else
#define p2j(x) ((jlong)(uintptr_t)(x))
#define j2p(x, type) ((type)(uintptr_t)(x))
#define j2c(x, type) ((type*)(uintptr_t)(x))
#define j2vp(x) j2c(x, void)
#define MIN(x, y) ((x) < (y) ? (x) : (y))
#define EXTERNC extern
#endif

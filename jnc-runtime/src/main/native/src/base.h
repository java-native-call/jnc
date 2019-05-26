#pragma once

#include <jni.h>

#ifdef _WIN32
// attribute hidden is not supported on windows
#define JNC_SYMBOL_HIDDEN
#elif __GNUC__ >= 4
// works for both gcc and clang
#define JNC_SYMBOL_HIDDEN __attribute__ ((visibility ("hidden")))
#else // not defined(__GNUC__) or __GNUC__ < 4
#define JNC_SYMBOL_HIDDEN
#endif // __GNUC__

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

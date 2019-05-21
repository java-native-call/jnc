#pragma once

#include <jnc_foreign_internal_NativeMethods.h>
#include <ffi.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include <wchar.h>

#define JAVA_LANG_STR(name)     "java/lang/" #name
#define ArrayIndexOutOfBounds   JAVA_LANG_STR(ArrayIndexOutOfBoundsException)
#define IllegalArgument         JAVA_LANG_STR(IllegalArgumentException)
#define NullPointer             JAVA_LANG_STR(NullPointerException)
#define OutOfMemory             JAVA_LANG_STR(OutOfMemoryError)
#define UnknownError            JAVA_LANG_STR(UnknownError)
#define UnsatisfiedLink         JAVA_LANG_STR(UnsatisfiedLinkError)
#define SIG_STRING              "L" JAVA_LANG_STR(String) ";"

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

#define throwByName(env, name, msg)             \
do {                                            \
    jclass _jc = CALLJNI(env, FindClass, name); \
    CALLJNI(env, ThrowNew, _jc, msg);           \
    CALLJNI(env, DeleteLocalRef, _jc);          \
} while(false)
#define throwByNameA(key, sig, env, name, value)                            \
do {                                                                        \
    jclass _jc = CALLJNI(env, FindClass, name);                             \
    jmethodID _jm = CALLJNI(env, GetMethodID, _jc, "<init>", "(" sig ")V"); \
    jvalue _jv;                                                             \
    _jv.key = value;                                                        \
    jobject _jo = CALLJNI(env, NewObjectA, _jc, _jm, &_jv);                 \
    CALLJNI(env, Throw, _jo);                                               \
    CALLJNI(env, DeleteLocalRef, _jo);                                      \
    CALLJNI(env, DeleteLocalRef, _jc);                                      \
} while(false)
#define throwByNameS(...) throwByNameA(l, SIG_STRING, __VA_ARGS__)
#define checkError(type, env, name, ret)    \
do {                                        \
    if (unlikely(NULL == name)) {           \
        throwByName(env, type, NULL);       \
        return ret;                         \
    }                                       \
} while(false)

#define checkIllegalArgument(env, condition, ret)       \
do {                                                    \
    if (unlikely(!(condition))) {                       \
        throwByName(env, IllegalArgument, #condition);  \
        return ret;                                     \
    }                                                   \
} while(false)

#define checkNullPointer(...)   checkError(NullPointer, __VA_ARGS__)
#define checkOutOfMemory(...)   checkError(OutOfMemory, __VA_ARGS__)

#define DO_WITH_STRING_UTF(env, jstring, name, stat, ret)       \
do {                                                            \
    jsize _len = CALLJNI(env, GetStringUTFLength, jstring);     \
    char* name = (char*) malloc(_len + 1);                      \
    checkOutOfMemory(env, name, ret);                           \
    CALLJNI(env, GetStringUTFRegion, jstring, 0, _len, name);   \
    name[_len] = 0;                                             \
    stat;                                                       \
    free(name);                                                 \
} while(false)

#ifdef UNUSED
/* nothing */
#elif defined(__GNUC__)
#define UNUSED(x) UNUSED_ ## x __attribute__((unused))
#elif defined(__LCLINT__)
#define UNUSED(x) /*@unused@*/ x
#else    /* !__GNUC__ && !__LCLINT__ */
#define UNUSED(x) x
#endif   /* !__GNUC__ && !__LCLINT__ */

#define NOOP(...) __VA_ARGS__
#define p2j(x) ((jlong)(uintptr_t)(x))
#define c2j(x) p2j(&x)
#define j2p(x, type) ((type)(uintptr_t)(x))
#define j2c(x, type) j2p(x, type*)
#define j2vp(x) j2c(x, void)

#define MIN(x, y) ((x) < (y) ? (x) : (y))

#ifdef __GNUC__
#define likely(x) __builtin_expect((x), 1)
#define unlikely(x) __builtin_expect((x), 0)
#else
#define likely(x) (x)
#define unlikely(x) (x)
#endif

#define JNC_TYPE(type) jnc_foreign_internal_NativeMethods_TYPE_##type
#define CHECK_JNC_FFI(type) (JNC_TYPE(type) != FFI_TYPE_##type)
#if \
CHECK_JNC_FFI(VOID) || \
CHECK_JNC_FFI(INT) || \
CHECK_JNC_FFI(FLOAT) || \
CHECK_JNC_FFI(DOUBLE) || \
CHECK_JNC_FFI(UINT8) || \
CHECK_JNC_FFI(SINT8) || \
CHECK_JNC_FFI(UINT16) || \
CHECK_JNC_FFI(SINT16) || \
CHECK_JNC_FFI(UINT32) || \
CHECK_JNC_FFI(SINT32) || \
CHECK_JNC_FFI(UINT64) || \
CHECK_JNC_FFI(SINT64) || \
CHECK_JNC_FFI(STRUCT) || \
CHECK_JNC_FFI(POINTER)
#error out of sync with ffi.h
#endif
#undef CHECK_JNC_FFI

#define JNC_CALL(type) jnc_foreign_internal_NativeMethods_CONVENTION_##type

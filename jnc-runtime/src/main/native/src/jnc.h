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

#define throwByName(env, name, msg)                    \
do {                                                   \
    jclass jc_ = CALLJNI(env, FindClass, name);        \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break; \
    CALLJNI(env, ThrowNew, jc_, msg);                  \
    CALLJNI(env, DeleteLocalRef, jc_);                 \
} while(false)
#define throwByNameA(key, sig, env, name, value)                            \
do {                                                                        \
    jclass jc_ = CALLJNI(env, FindClass, name);                             \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break;                      \
    jmethodID _jm = CALLJNI(env, GetMethodID, jc_, "<init>", "(" sig ")V"); \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break;                      \
    jvalue jv_;                                                             \
    jv_.key = value;                                                        \
    jobject jo_ = CALLJNI(env, NewObjectA, jc_, _jm, &jv_);                 \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break;                      \
    CALLJNI(env, Throw, jo_);                                               \
    CALLJNI(env, DeleteLocalRef, jo_);                                      \
    CALLJNI(env, DeleteLocalRef, jc_);                                      \
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

/* GetStringChars is not guaranteed to be null terminated
   especially on old jdk */
#define DO_WITH_STRING_16(env, jstring, name, length, stat, ret)      \
do {                                                                  \
    jsize length = CALLJNI(env, GetStringLength, jstring);            \
    if (unlikely(CALLJNI(env, ExceptionCheck))) return ret;           \
    jchar* name = (jchar*) malloc((length + 1) * sizeof (jchar));     \
    checkOutOfMemory(env, name, ret);                                 \
    CALLJNI(env, GetStringRegion, jstring, 0, length, (jchar*) name); \
    if (unlikely(CALLJNI(env, ExceptionCheck))) return ret;           \
    name[length] = 0;                                                 \
    stat;                                                             \
    free(name);                                                       \
} while(false)

#define DO_WITH_STRING_UTF(env, jstring, name, length, stat, ret) \
do {                                                              \
    jsize length = CALLJNI(env, GetStringUTFLength, jstring);     \
    if (unlikely(CALLJNI(env, ExceptionCheck))) return ret;       \
    char* name = (char*) malloc(length + 1);                      \
    checkOutOfMemory(env, name, ret);                             \
    CALLJNI(env, GetStringUTFRegion, jstring, 0, length, name);   \
    if (unlikely(CALLJNI(env, ExceptionCheck))) return ret;       \
    name[length] = 0;                                             \
    stat;                                                         \
    free(name);                                                   \
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
#define likely(x) __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)
#else
#define likely(x) (x)
#define unlikely(x) (x)
#endif

#define JNC_TYPE(type) jnc_foreign_internal_NativeMethods_TYPE_##type
#define CHECK_JNC_FFI(type) (JNC_TYPE(type) != FFI_TYPE_##type)
#if \
CHECK_JNC_FFI(VOID) || \
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

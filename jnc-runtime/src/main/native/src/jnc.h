#pragma once

#include "jnc_foreign_internal_NativeAccessor.h"
#include "jnc_foreign_internal_NativeMethods.h"
#include <ffi.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include <wchar.h>
#include "exception.h"

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
#ifdef __cplusplus
#define p2j(x) jlong(reinterpret_cast<uintptr_t>(x))
#define j2p(x, type) reinterpret_cast<type>(uintptr_t(x))
#define j2c(x, type) j2p(x, type*)
#define j2vp(x) j2c(x, void)
#else
#define p2j(x) ((jlong)(uintptr_t)(x))
#define j2p(x, type) ((type)(uintptr_t)(x))
#define j2c(x, type) j2p(x, type*)
#define j2vp(x) j2c(x, void)
#endif

#define MIN(x, y) ((x) < (y) ? (x) : (y))

#define JNC_TYPE(type) jnc_foreign_internal_NativeAccessor_TYPE_##type
#define CHECK_JNC_FFI(type) (JNC_TYPE(type) == FFI_TYPE_##type)
#if \
CHECK_JNC_FFI(VOID) && \
CHECK_JNC_FFI(FLOAT) && \
CHECK_JNC_FFI(DOUBLE) && \
CHECK_JNC_FFI(UINT8) && \
CHECK_JNC_FFI(SINT8) && \
CHECK_JNC_FFI(UINT16) && \
CHECK_JNC_FFI(SINT16) && \
CHECK_JNC_FFI(UINT32) && \
CHECK_JNC_FFI(SINT32) && \
CHECK_JNC_FFI(UINT64) && \
CHECK_JNC_FFI(SINT64) && \
CHECK_JNC_FFI(STRUCT) && \
CHECK_JNC_FFI(POINTER)
#else
#error out of sync with ffi.h
#endif
#undef CHECK_JNC_FFI

#define JNC_CALL(type) jnc_foreign_internal_NativeAccessor_CONVENTION_##type

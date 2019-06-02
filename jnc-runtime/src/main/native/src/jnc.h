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

/* GetStringChars is not guaranteed to be null terminated */
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
    jsize strLen_ = CALLJNI(env, GetStringLength, jstring);       \
    if (unlikely(CALLJNI(env, ExceptionCheck))) return ret;       \
    char *name = (char*) malloc(length + 1);                      \
    checkOutOfMemory(env, name, ret);                             \
    CALLJNI(env, GetStringUTFRegion, jstring, 0, strLen_, name);  \
    if (unlikely(CALLJNI(env, ExceptionCheck))) return ret;       \
    name[length] = 0;                                             \
    stat;                                                         \
    free(name);                                                   \
} while(false)

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

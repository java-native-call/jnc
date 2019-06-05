#pragma once

#include "jnc_foreign_internal_NativeAccessor.h"
#include "jnc_foreign_internal_NativeMethods.h"
#include <ffi.h>
#include "exception.h"

#define JNC_CALL(type) jnc_foreign_internal_NativeAccessor_CONVENTION_##type
#define JNC_RTLD(name) jnc_foreign_internal_NativeAccessor_RTLD_##name
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

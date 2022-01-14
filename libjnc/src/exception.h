#pragma once

#include "base.h"

#define ArrayIndexOutOfBounds   jnc_array_index_out_of_bounds
#define IllegalArgument         jnc_illegal_argument
#define NullPointer             jnc_null_pointer
#define OutOfMemory             jnc_out_of_memory
#define UnknownError            jnc_unknown_error
#define UnsatisfiedLink         jnc_unsatisfied_link

#ifdef __cplusplus
EXTERNC {
#endif
JNC_SYMBOL_HIDDEN extern const char *jnc_array_index_out_of_bounds;
JNC_SYMBOL_HIDDEN extern const char *jnc_illegal_argument;
JNC_SYMBOL_HIDDEN extern const char *jnc_null_pointer;
JNC_SYMBOL_HIDDEN extern const char *jnc_out_of_memory;
JNC_SYMBOL_HIDDEN extern const char *jnc_unknown_error;
JNC_SYMBOL_HIDDEN extern const char *jnc_unsatisfied_link;

JNC_SYMBOL_HIDDEN void jnc_throw_by_name(JNIEnv *, const char *, const char *);

#ifdef __cplusplus
}
#endif

#define throwByName(...) jnc_throw_by_name(__VA_ARGS__)

#if __cplusplus >= 201103L
#define checkError(type, env, name, ret) \
do { if (unlikely(nullptr == (name))) { throwByName(env, type, nullptr); return ret; } } while (false)
#else
#define checkError(type, env, name, ret) \
do { if (unlikely(NULL == (name))) { throwByName(env, type, NULL); return ret; } } while (false)
#endif

#define checkNullPointer(...)   checkError(NullPointer, __VA_ARGS__)
#define checkOutOfMemory(...)   checkError(OutOfMemory, __VA_ARGS__)

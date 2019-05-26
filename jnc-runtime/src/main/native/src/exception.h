#pragma once

#include "base.h"

#define JAVA_LANG_STR(name)     "java/lang/" #name
#define ArrayIndexOutOfBounds   jnc_array_index_out_of_bounds
#define IllegalArgument         jnc_illegal_argument
#define NullPointer             jnc_null_pointer
#define OutOfMemory             jnc_out_of_memory
#define UnknownError            jnc_unknown_error
#define UnsatisfiedLink         jnc_unsatisfied_link

#define throwByName(...) jnc_throw_by_name(__VA_ARGS__)

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

#ifdef __cplusplus
extern "C" {
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

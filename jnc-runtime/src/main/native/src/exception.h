#pragma once

#ifdef _WIN32
// attribute hidden is not supported on windows
#define JNC_SYMBOL_HIDDEN
#elif __GNUC__ >= 4
// works for both gcc and clang
#define JNC_SYMBOL_HIDDEN __attribute__ ((visibility ("hidden")))
#else // not defined(__GNUC__) or __GNUC__ < 4
#define JNC_SYMBOL_HIDDEN
#endif // __GNUC__

#define JAVA_LANG_STR(name)     "java/lang/" #name
#define ArrayIndexOutOfBounds   jnc_array_index_out_of_bounds
#define IllegalArgument         jnc_illegal_argument
#define NullPointer             jnc_null_pointer
#define OutOfMemory             jnc_out_of_memory
#define UnknownError            jnc_unknown_error
#define UnsatisfiedLink         jnc_unsatisfied_link

#ifdef __cplusplus
extern "C" {
#endif
JNC_SYMBOL_HIDDEN extern const char *jnc_array_index_out_of_bounds;
JNC_SYMBOL_HIDDEN extern const char *jnc_illegal_argument;
JNC_SYMBOL_HIDDEN extern const char *jnc_null_pointer;
JNC_SYMBOL_HIDDEN extern const char *jnc_out_of_memory;
JNC_SYMBOL_HIDDEN extern const char *jnc_unknown_error;
JNC_SYMBOL_HIDDEN extern const char *jnc_unsatisfied_link;
#ifdef __cplusplus
}
#endif

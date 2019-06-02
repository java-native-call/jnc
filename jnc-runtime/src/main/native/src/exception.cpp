#include "exception.h"

const char *jnc_array_index_out_of_bounds = "java/lang/ArrayIndexOutOfBoundsException";
const char *jnc_illegal_argument = "java/lang/IllegalArgumentException";
const char *jnc_null_pointer = "java/lang/NullPointerException";
const char *jnc_out_of_memory = "java/lang/OutOfMemoryError";
const char *jnc_unknown_error = "java/lang/UnknownError";
const char *jnc_unsatisfied_link = "java/lang/UnsatisfiedLinkError";

void jnc_throw_by_name(JNIEnv *env, const char *name, const char *msg) {
    auto type = CALLJNI(env, FindClass, name);
    if (unlikely(CALLJNI(env, ExceptionCheck))) return;
    CALLJNI(env, ThrowNew, type, msg);
    CALLJNI(env, DeleteLocalRef, type);
}

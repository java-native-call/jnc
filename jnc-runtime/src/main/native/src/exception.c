#include "exception.h"

const char *jnc_array_index_out_of_bounds = JAVA_LANG_STR(ArrayIndexOutOfBoundsException);
const char *jnc_illegal_argument = JAVA_LANG_STR(IllegalArgumentException);
const char *jnc_null_pointer = JAVA_LANG_STR(NullPointerException);
const char *jnc_out_of_memory = JAVA_LANG_STR(OutOfMemoryError);
const char *jnc_unknown_error = JAVA_LANG_STR(UnknownError);
const char *jnc_unsatisfied_link = JAVA_LANG_STR(UnsatisfiedLinkError);

void jnc_throw_by_name(JNIEnv *env, const char *name, const char *msg) {
    jclass class = CALLJNI(env, FindClass, name);
    if (unlikely(CALLJNI(env, ExceptionCheck))) return;
    CALLJNI(env, ThrowNew, class, msg);
    CALLJNI(env, DeleteLocalRef, class);
}

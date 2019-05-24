#include "jnc.h"

static inline bool checkNullAndRange(JNIEnv *env, jarray array, jint offset, jint size) {
    checkNullPointer(env, array, true);
    if (unlikely(offset < 0 || offset > CALLJNI(env, GetArrayLength, array) - size)) {
        throwByName(env, ArrayIndexOutOfBounds, NULL);
        return true;
    }
    return false;
}

#define ACCESS_ADDRESS_ARRAY(atype, jni, fname)             \
JNIEXPORT void JNICALL                                      \
Java_jnc_foreign_internal_NativeMethods_get##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (unlikely(checkNullAndRange(env, array, off, len)))  \
        return;                                             \
    CALLJNI(env, Set##jni##ArrayRegion, array, off,         \
        len, addr);                                         \
}                                                           \
JNIEXPORT void JNICALL                                      \
Java_jnc_foreign_internal_NativeMethods_put##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (unlikely(checkNullAndRange(env, array, off, len)))  \
        return;                                             \
    CALLJNI(env, Get##jni##ArrayRegion,                     \
        array, off, len, addr);                             \
}
ACCESS_ADDRESS_ARRAY(jbyte, Byte, Bytes);
ACCESS_ADDRESS_ARRAY(jshort, Short, ShortArray);
ACCESS_ADDRESS_ARRAY(jchar, Char, CharArray);
ACCESS_ADDRESS_ARRAY(jint, Int, IntArray);
ACCESS_ADDRESS_ARRAY(jlong, Long, LongArray);
ACCESS_ADDRESS_ARRAY(jfloat, Float, FloatArray);
ACCESS_ADDRESS_ARRAY(jdouble, Double, DoubleArray);

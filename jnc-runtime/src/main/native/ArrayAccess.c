#include "jnc.h"

static inline bool checkNullAndRange(JNIEnv * env, jarray array, jint offset, jint size) {
    checkNullPointer(env, array, true);
    if (unlikely(offset < 0 || offset > (*env)->GetArrayLength(env, array) - size)) {
        throwByName(env, ArrayIndexOutOfBounds, NULL);
        return true;
    }
    return false;
}

#define ARRAY_RAW_ACCESS_E(jni, atype, name, native, jtype, j2n, n2j)   \
union atype##array_AND_##name {                                         \
    atype array[sizeof (native) / sizeof (atype)];                      \
    native value;                                                       \
};                                                                      \
JNIEXPORT void JNICALL                                                  \
Java_jnc_foreign_internal_NativeMethods_put##jni##sRaw##name            \
(JNIEnv *env, jobject UNUSED(self), atype##Array array,                 \
        jint offset, jtype value) {                                     \
    union atype##array_AND_##name v;                                    \
    int size = sizeof(native) / sizeof(atype);                          \
    if (unlikely(checkNullAndRange(env, array, offset, size)))          \
        return;                                                         \
    v.value = j2n(value);                                               \
    (*env)->Set##jni##ArrayRegion                                       \
        (env, array, offset, size, v.array);                            \
}                                                                       \
JNIEXPORT jtype JNICALL                                                 \
Java_jnc_foreign_internal_NativeMethods_get##jni##sRaw##name            \
(JNIEnv *env, jobject UNUSED(self), atype##Array array, jint offset) {  \
    union atype##array_AND_##name v;                                    \
    int size = sizeof(native) / sizeof(atype);                          \
    if (unlikely(checkNullAndRange(env, array, offset, size)))          \
        return 0;                                                       \
    (*env)->Get##jni##ArrayRegion(env, array, offset, size, v.array);   \
    return n2j(v.value);                                                \
}

#define ARRAY_RAW_ACCESS(jni, atype, name, jtype) \
    ARRAY_RAW_ACCESS_E(jni, atype, name, jtype, jtype, NOOP, NOOP)
#define BYTE_ARRAY_RAW_ACCESS_E(...) ARRAY_RAW_ACCESS_E(Byte, jbyte, __VA_ARGS__)
#define BYTE_ARRAY_RAW_ACCESS(...) ARRAY_RAW_ACCESS(Byte, jbyte, __VA_ARGS__)

BYTE_ARRAY_RAW_ACCESS(Short, jshort);
BYTE_ARRAY_RAW_ACCESS(Char, jchar);
BYTE_ARRAY_RAW_ACCESS(Int, jint);
BYTE_ARRAY_RAW_ACCESS(Long, jlong);
BYTE_ARRAY_RAW_ACCESS(Float, jfloat);
BYTE_ARRAY_RAW_ACCESS(Double, jdouble);
BYTE_ARRAY_RAW_ACCESS_E(Address, void*, jlong, j2vp, p2j);

#define ACCESS_ADDRESS_ARRAY(atype, jni, fname)             \
JNIEXPORT void JNICALL                                      \
Java_jnc_foreign_internal_NativeMethods_get##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (unlikely(checkNullAndRange(env, array, off, len)))  \
        return;                                             \
    (*env)->Set##jni##ArrayRegion(env, array, off,          \
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
    (*env)->Get##jni##ArrayRegion(env,                      \
        array, off, len, addr);                             \
}
ACCESS_ADDRESS_ARRAY(jbyte, Byte, Bytes);
ACCESS_ADDRESS_ARRAY(jshort, Short, ShortArray);
ACCESS_ADDRESS_ARRAY(jchar, Char, CharArray);
ACCESS_ADDRESS_ARRAY(jint, Int, IntArray);
ACCESS_ADDRESS_ARRAY(jlong, Long, LongArray);
ACCESS_ADDRESS_ARRAY(jfloat, Float, FloatArray);
ACCESS_ADDRESS_ARRAY(jdouble, Double, DoubleArray);

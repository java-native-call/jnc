#include "jnc.h"
#include "convert.h"

static bool checkNullAndRange(JNIEnv *env, jarray array, jint offset, jint size) {
    checkNullPointer(env, array, false);
    if (unlikely(offset < 0 || offset > CALLJNI(env, GetArrayLength, array) - size)) {
        throwByName(env, ArrayIndexOutOfBounds, nullptr);
        return false;
    }
    return true;
}

#define ADDRESS_ARRAY_ACCESS(atype, jni, fname)             \
EXTERNC JNIEXPORT void JNICALL                              \
Java_jnc_foreign_internal_NativeMethods_get##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (likely(checkNullAndRange(env, array, off, len)))    \
        CALLJNI(env, Set##jni##ArrayRegion, array, off,     \
            len, addr);                                     \
}                                                           \
EXTERNC JNIEXPORT void JNICALL                              \
Java_jnc_foreign_internal_NativeMethods_put##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (likely(checkNullAndRange(env, array, off, len)))    \
        CALLJNI(env, Get##jni##ArrayRegion,                 \
            array, off, len, addr);                         \
}

ADDRESS_ARRAY_ACCESS(jbyte, Byte, Bytes);
ADDRESS_ARRAY_ACCESS(jshort, Short, ShortArray);
ADDRESS_ARRAY_ACCESS(jchar, Char, CharArray);
ADDRESS_ARRAY_ACCESS(jint, Int, IntArray);
ADDRESS_ARRAY_ACCESS(jlong, Long, LongArray);
ADDRESS_ARRAY_ACCESS(jfloat, Float, FloatArray);
ADDRESS_ARRAY_ACCESS(jdouble, Double, DoubleArray);

#define DEFINE_PUTTER(name, jtype)                  \
EXTERNC JNIEXPORT void JNICALL                      \
Java_jnc_foreign_internal_NativeMethods_put##name   \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,   \
        jlong ltype, jtype value) {                 \
    void * paddr = j2vp(laddr);                     \
    ffi_type *ptype = j2c(ltype, ffi_type);         \
    checkNullPointer(env, paddr, /*void*/);         \
    checkNullPointer(env, ptype, /*void*/);         \
    PUT_ALL(ptype, paddr, value);                   \
}

DEFINE_PUTTER(Int, jint);
DEFINE_PUTTER(Long, jlong);
DEFINE_PUTTER(Float, jfloat);
DEFINE_PUTTER(Double, jdouble);

#define DEFINE_GETTER(name, jtype)                                  \
EXTERNC JNIEXPORT jtype JNICALL                                     \
Java_jnc_foreign_internal_NativeMethods_get##name                   \
(JNIEnv * env, jobject UNUSED(self), jlong laddr, jlong ltype) {    \
    void * paddr = j2vp(laddr);                                     \
    ffi_type * ptype = j2c(ltype, ffi_type);                        \
    checkNullPointer(env, paddr, 0);                                \
    checkNullPointer(env, ptype, 0);                                \
    RET_##jtype(ptype, paddr);                                      \
}

DEFINE_GETTER(Boolean, jboolean);
DEFINE_GETTER(Int, jint);
DEFINE_GETTER(Long, jlong);
DEFINE_GETTER(Float, jfloat);
DEFINE_GETTER(Double, jdouble);

#define ADDRESS_ACCESS_E(name, native, jtype, j2n, n2j)             \
EXTERNC JNIEXPORT void JNICALL                                      \
Java_jnc_foreign_internal_NativeMethods_putRaw##name                \
(JNIEnv *env, jobject UNUSED(self), jlong address, jtype value) {   \
    native * paddr = j2c(address, native);                          \
    checkNullPointer(env, paddr, /*void*/);                         \
    *paddr = j2n(value);                                            \
}                                                                   \
EXTERNC JNIEXPORT jtype JNICALL                                     \
Java_jnc_foreign_internal_NativeMethods_getRaw##name                \
(JNIEnv *env, jobject UNUSED(self), jlong address) {                \
    native * paddr = j2c(address, native);                          \
    checkNullPointer(env, paddr, 0);                                \
    return n2j(*paddr);                                             \
}

#define ADDRESS_ACCESS(name, jtype) \
    ADDRESS_ACCESS_E(name, jtype, jtype, NOOP, NOOP)
ADDRESS_ACCESS(Byte, jbyte);
ADDRESS_ACCESS(Short, jshort);
ADDRESS_ACCESS(Char, jchar);
ADDRESS_ACCESS(Int, jint);
ADDRESS_ACCESS(Long, jlong);
ADDRESS_ACCESS(Float, jfloat);
ADDRESS_ACCESS(Double, jdouble);
ADDRESS_ACCESS_E(Address, void*, jlong, j2vp, p2j);

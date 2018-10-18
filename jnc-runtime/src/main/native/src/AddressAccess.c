#include "jnc.h"
#include "types.h"

#define DEFINE_PUTTER(name, jtype)                  \
JNIEXPORT void JNICALL                              \
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
JNIEXPORT jtype JNICALL                                             \
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

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    putStringUTF
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_putStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jstring value) {
    void *paddr = j2vp(laddr);
    checkNullPointer(env, paddr, /*void*/);
    checkNullPointer(env, value, /*void*/);
    DO_WITH_STRING_UTF(env, value, str, memcpy(paddr, str, (size_t) (_len + 1)), /*void*/);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTF
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, NULL);
    return CALLJNI(env, NewStringUTF, paddr);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTFN
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTFN
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    if (unlikely(limit <= 0)) {
        if (likely(limit == 0)) {
            return CALLJNI(env, NewStringUTF, "");
        }
        throwByName(env, IllegalArgument, "limit>=0");
        return NULL;
    }
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, NULL);
    size_t szLimit = (size_t) MIN(limit, (jlong) ((SIZE_MAX >> 1) - 1));
    if (likely(NULL != memchr(paddr, 0, szLimit))) {
        return CALLJNI(env, NewStringUTF, paddr);
    }
    char *tmp = (char *) malloc(szLimit + 1);
    checkOutOfMemory(env, tmp, NULL);
    memcpy(tmp, paddr, szLimit);
    tmp[szLimit] = 0;
    jstring result = CALLJNI(env, NewStringUTF, tmp);
    free(tmp);
    return result;
}

#define ADDRESS_ACCESS_E(name, native, jtype, j2n, n2j)             \
JNIEXPORT void JNICALL                                              \
Java_jnc_foreign_internal_NativeMethods_putRaw##name                \
(JNIEnv *env, jclass UNUSED(class), jlong address, jtype value) {   \
    native * paddr = j2c(address, native);                          \
    checkNullPointer(env, paddr, /*void*/);                         \
    *paddr = j2n(value);                                            \
}                                                                   \
JNIEXPORT jtype JNICALL                                             \
Java_jnc_foreign_internal_NativeMethods_getRaw##name                \
(JNIEnv *env, jclass UNUSED(class), jlong address) {                \
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

#include "jnc.h"

/**
 * return address of this field if require malloc(0)
 */
static jlong malloc_zero;

#define checkJlongIsSizeT(env, var, throwOnLarge, ret)  \
do {                                                    \
    if (unlikely(var < 0)) {                            \
        throwByName(env, IllegalArgument, nullptr);     \
        return ret;                                     \
    }                                                   \
    if (unlikely(jlong(size_t(var)) != var)) {          \
        throwByName(env, throwOnLarge, nullptr);        \
        return ret;                                     \
    }                                                   \
} while(false)

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    allocateMemory
 * Signature: (J)J
 */
EXTERNC JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_allocateMemory
(JNIEnv *env, jobject UNUSED(self), jlong size) {
    checkJlongIsSizeT(env, size, OutOfMemory, 0);
    if (unlikely(size == 0)) return p2j(&malloc_zero);
    void *ret = malloc((size_t) size);
    checkOutOfMemory(env, ret, 0);
    return p2j(memset(ret, 0, (size_t) size));
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    copyMemory
 * Signature: (JJJ)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_copyMemory
(JNIEnv *env, jobject UNUSED(self), jlong ldst, jlong lsrc, jlong n) {
    checkJlongIsSizeT(env, n, IllegalArgument, /*void*/);
    void *pdst = j2vp(ldst);
    void *psrc = j2vp(lsrc);
    checkNullPointer(env, pdst, /*void*/);
    checkNullPointer(env, psrc, /*void*/);
    memcpy(pdst, psrc, (size_t) n);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    freeMemory
 * Signature: (J)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_freeMemory
(JNIEnv *UNUSED(env), jobject UNUSED(self), jlong laddr) {
    /* free(nullptr) should be noop, it's a good habbit to check null */
    void *paddr = j2vp(laddr);
    if (likely(nullptr != paddr && paddr != &malloc_zero)) {
        free(paddr);
    }
}

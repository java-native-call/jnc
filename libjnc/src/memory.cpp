#include "jnc.h"

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
 * Class:     jnc_provider_NativeMethods
 * Method:    allocateMemory
 * Signature: (J)J
 */
EXTERNC JNIEXPORT jlong JNICALL
Java_jnc_provider_NativeMethods_allocateMemory
(JNIEnv *env, jobject UNUSED(self), jlong size) {
    checkJlongIsSizeT(env, size, OutOfMemory, 0);
    // Maybe malloc(0) returns null on some platform.
    if (unlikely(size == 0)) size = 1;
    void *ret = malloc((size_t) size);
    checkOutOfMemory(env, ret, 0);
    return p2j(memset(ret, 0, (size_t) size));
}

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    copyMemory
 * Signature: (JJJ)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_provider_NativeMethods_copyMemory
(JNIEnv *env, jobject UNUSED(self), jlong ldst, jlong lsrc, jlong n) {
    checkJlongIsSizeT(env, n, IllegalArgument, /*void*/);
    void *pdst = j2vp(ldst);
    void *psrc = j2vp(lsrc);
    checkNullPointer(env, pdst, /*void*/);
    checkNullPointer(env, psrc, /*void*/);
    memcpy(pdst, psrc, (size_t) n);
}

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    freeMemory
 * Signature: (J)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_provider_NativeMethods_freeMemory
(JNIEnv *UNUSED(env), jobject UNUSED(self), jlong laddr) {
    /* free(nullptr) should be noop, it's a good habbit to check null */
    void *paddr = j2vp(laddr);
    if (likely(nullptr != paddr)) {
        free(paddr);
    }
}

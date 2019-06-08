#include "jnc.h"
#include "commons.h"

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    allocateMemory
 * Signature: (J)J
 */
EXTERNC JNIEXPORT jlong JNICALL
Java_jnc_provider_NativeMethods_allocateMemory
(JNIEnv *env, jobject UNUSED(self), jlong size) {
    if (unlikely(size < 0)) {
        throwByName(env, IllegalArgument, nullptr);
        return 0;
    }
    if (unlikely(is_sizet_large_enough(size))) {
        throwByName(env, OutOfMemory, nullptr);
        return 0;
    }
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
    if (unlikely(n < 0)) {
        throwByName(env, IllegalArgument, nullptr);
        return;
    }
    if (unlikely(is_sizet_large_enough(n))) {
        throwByName(env, IllegalArgument, nullptr);
        return;
    }
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

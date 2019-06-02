#include "jnc.h"

/**
 * return address of this field if require malloc(0)
 */
static jlong malloc_zero;

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    allocateMemory
 * Signature: (J)J
 */
EXTERNC JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_allocateMemory
(JNIEnv *env, jobject UNUSED(self), jlong size) {
    if (unlikely(size < 0)) {
        throwByName(env, IllegalArgument, NULL);
        return 0;
    }
    if (unlikely((jlong) (size_t) size != size)) {
        throwByName(env, OutOfMemory, NULL);
        return 0;
    }
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
    if (unlikely(n < 0)) {
        throwByName(env, IllegalArgument, NULL);
        return;
    }
    size_t sizetN = size_t(n);
    if (
            NOT_LP64(jlong(sizetN) != n)
            LP64_ONLY(false)
            ) {
        throwByName(env, IllegalArgument, NULL);
        return;
    }
    void *pdst = j2vp(ldst);
    void *psrc = j2vp(lsrc);
    checkNullPointer(env, pdst, /*void*/);
    checkNullPointer(env, psrc, /*void*/);
    memcpy(pdst, psrc, sizetN);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    freeMemory
 * Signature: (J)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_freeMemory
(JNIEnv *UNUSED(env), jobject UNUSED(self), jlong laddr) {
    /* free(NULL) should be noop, it's a good habbit to check null */
    void *paddr = j2vp(laddr);
    if (likely(NULL != paddr && paddr != &malloc_zero)) {
        free(paddr);
    }
}

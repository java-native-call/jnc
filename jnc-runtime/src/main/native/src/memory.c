#include "jnc.h"

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    allocateMemory
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_allocateMemory
(JNIEnv *env, jobject UNUSED(self), jlong size) {
    checkIllegalArgument(env, size >= 0, 0);
    if (unlikely((jlong) (size_t) size != size)) {
        throwByName(env, OutOfMemory, NULL);
        return 0;
    }
    if (unlikely(size == 0)) size = 1;
    void *ret = malloc((size_t) size);
    checkOutOfMemory(env, ret, 0);
    return p2j(memset(ret, 0, (size_t) size));
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    copyMemory
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_copyMemory
(JNIEnv *env, jobject UNUSED(self), jlong ldst, jlong lsrc, jlong n) {
    if (unlikely(n <= 0 || (jlong) (size_t) n != n)) {
        if (n != 0) {
            throwByName(env, IllegalArgument, NULL);
        }
        return;
    }
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
JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_freeMemory
(JNIEnv *UNUSED(env), jobject UNUSED(self), jlong laddr) {
    /* free(NULL) should be noop, it's a good habbit to check null */
    void *paddr = j2vp(laddr);
    if (likely(NULL != paddr)) {
        free(paddr);
    }
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    strlen
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_strlen
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, 0);
    return strlen(paddr);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    wcslen
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_wcslen
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    wchar_t *paddr = j2c(laddr, wchar_t);
    checkNullPointer(env, paddr, 0);
    return wcslen(paddr);
}

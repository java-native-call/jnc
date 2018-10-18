#include "jnc.h"

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getBufferAddress
 * Signature: (Ljava/nio/Buffer;)J
 */
JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_getBufferAddress
(JNIEnv *env, jobject UNUSED(self), jobject buf) {
    checkNullPointer(env, buf, 0);
    void *addr = CALLJNI(env, GetDirectBufferAddress, buf);
    if (unlikely(NULL == addr)) {
        throwByName(env, IllegalArgument, NULL);
        return 0;
    }
    return p2j(addr);
}

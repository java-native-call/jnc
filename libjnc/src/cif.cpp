#include "jnc.h"

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getCifInfo
 * Signature: ()J
 */
EXTERNC JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_getCifInfo
(JNIEnv *UNUSED(env), jobject UNUSED(self)) {
    auto size = sizeof(ffi_cif);
    auto align = alignof(ffi_cif);

    return (uint64_t(uint32_t(align)) << 32) | uint32_t(size);
}

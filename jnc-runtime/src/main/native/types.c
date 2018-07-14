#include "jnc.h"

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    findType
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_findType
(JNIEnv *env, jobject UNUSED(self), jint type) {
    switch (type) {
#define F(id, value) case JNC_TYPE(id): return c2j(ffi_type_##value)
        F(VOID, void);
        F(FLOAT, float);
        F(DOUBLE, double);
        F(UINT8, uint8);
        F(SINT8, sint8);
        F(UINT16, uint16);
        F(SINT16, sint16);
        F(UINT32, uint32);
        F(SINT32, sint32);
        F(UINT64, uint64);
        F(SINT64, sint64);
        F(POINTER, pointer);
#undef F
    default:
        throwByName(env, IllegalArgument, NULL);
        return 0;
    }
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getTypeInfo
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_getTypeInfo
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    ffi_type *paddr = j2c(laddr, ffi_type);
    checkNullPointer(env, paddr, 0);
    return ((jlong) paddr->size << 32) | (paddr->alignment << 16) | (paddr->type);
}

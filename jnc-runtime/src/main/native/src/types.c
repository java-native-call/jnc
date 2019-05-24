#include "jnc.h"

#define ARRAY_LENGTH(x) (sizeof(x) / sizeof(x[0]))

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getTypes
 * Signature: ()[[J
 */
JNIEXPORT jobjectArray JNICALL Java_jnc_foreign_internal_NativeMethods_getTypes
(JNIEnv *env, jobject UNUSED(self)) {
#define F(value) &ffi_type_##value
    ffi_type * addr[] = {
        F(void), F(float), F(double), F(pointer),
        F(uint8), F(sint8), F(uint16), F(sint16),
        F(uint32), F(sint32), F(uint64), F(sint64),
#undef F
    };
    jclass longArray = (*env)->FindClass(env, "[J");
    if (unlikely((*env)->ExceptionCheck(env))) return NULL;
    jobjectArray res = (*env)->NewObjectArray(env, FFI_TYPE_LAST + 1, longArray, NULL);
    if (unlikely((*env)->ExceptionCheck(env))) return NULL;

    // require c99 if defined in for loop
    size_t i = 0;
    for (; i != ARRAY_LENGTH(addr); ++i) {
        ffi_type *p = addr[i];
        unsigned short type = p->type;
        jlong info = ((jlong) p->size << 32) | (p->alignment << 16) | type;
        jlongArray arr = (*env)->NewLongArray(env, 2);
        if (unlikely((*env)->ExceptionCheck(env))) return NULL;
        jlong region[2] = {p2j(p), info};
        (*env)->SetLongArrayRegion(env, arr, 0, 2, region);
        if (unlikely((*env)->ExceptionCheck(env))) return NULL;
        (*env)->SetObjectArrayElement(env, res, type, arr);
        if (unlikely((*env)->ExceptionCheck(env))) return NULL;
        (*env)->DeleteLocalRef(env, arr);
        if (unlikely((*env)->ExceptionCheck(env))) return NULL;
    }
    return res;
}

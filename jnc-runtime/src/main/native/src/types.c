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
    jclass longArray = CALLJNI(env, FindClass, "[J");
    if (unlikely(CALLJNI(env, ExceptionCheck))) return NULL;
    jobjectArray res = CALLJNI(env, NewObjectArray, FFI_TYPE_LAST + 1, longArray, NULL);
    if (unlikely(CALLJNI(env, ExceptionCheck))) return NULL;

    // require c99 if defined in for loop
    size_t i = 0;
    for (; i != ARRAY_LENGTH(addr); ++i) {
        ffi_type *p = addr[i];
        unsigned short type = p->type;
        jlong info = ((jlong) p->size << 32) | (p->alignment << 16) | type;
        jlongArray arr = CALLJNI(env, NewLongArray, 2);
        if (unlikely(CALLJNI(env, ExceptionCheck))) return NULL;
        jlong region[2] = {p2j(p), info};
        CALLJNI(env, SetLongArrayRegion, arr, 0, 2, region);
        if (unlikely(CALLJNI(env, ExceptionCheck))) return NULL;
        CALLJNI(env, SetObjectArrayElement, res, type, arr);
        if (unlikely(CALLJNI(env, ExceptionCheck))) return NULL;
        CALLJNI(env, DeleteLocalRef, arr);
    }
    return res;
}

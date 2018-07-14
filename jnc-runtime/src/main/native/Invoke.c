#include "jnc.h"
#include "types.h"

#ifdef _WIN32
#include <windows.h>
#else
#include <errno.h>
#define GetLastError() errno
#endif

static inline void saveLastError(JNIEnv *env, jobject obj, jlong methodId, int error) {
    if (likely(obj != NULL && methodId != 0)) {
        jmethodID method = j2p(methodId, jmethodID);
        jvalue v;
        v.i = error;
        (*env)->CallVoidMethodA(env, obj, method, &v);
    }
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    sizeof_ffi_cif
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_sizeof_1ffi_1cif
(JNIEnv *UNUSED(env), jobject UNUSED(self)) {
    return sizeof (ffi_cif);
}

#if defined(_WIN32) && !defined(_WIN64)
#define GET_ABI(x) ((x == JNC_CALL(STDCALL)) ? FFI_STDCALL : FFI_SYSV)
#else
#define GET_ABI(x) ((x & 0) | FFI_DEFAULT_ABI)
#endif

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    prepareInvoke
 * Signature: (JIIJJ)V
 */
JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_prepareInvoke
(JNIEnv *env, jobject UNUSED(self), jlong lcif, jint abi, jint nargs,
        jlong lrtype, jlong latype) {
    ffi_cif *pcif = j2c(lcif, ffi_cif);
    ffi_type *prtype = j2c(lrtype, ffi_type);
    ffi_type **patype = j2c(latype, ffi_type*);
    checkNullPointer(env, pcif, /*void*/);
    checkNullPointer(env, prtype, /*void*/);
    checkNullPointer(env, patype, /*void*/);
    switch (ffi_prep_cif(pcif, (ffi_abi) GET_ABI(abi), (unsigned) nargs, prtype, patype)) {
    case FFI_OK:
        break;
    case FFI_BAD_TYPEDEF:
        throwByName(env, IllegalArgument, "Bad typedef");
        break;
    case FFI_BAD_ABI:
        throwByName(env, IllegalArgument, "Bad abi");
        break;
    default:
        throwByName(env, UnknownError, NULL);
        break;
    }
}

#define DEFINE_INVOKE(name, jtype, ret)                 \
JNIEXPORT jtype JNICALL                                 \
Java_jnc_foreign_internal_NativeMethods_invoke##name    \
(JNIEnv * env, jobject UNUSED(self), jlong lcif,        \
        jlong jfun, jlong jav, jobject obj,             \
        jlong methodId) {                               \
    ffi_cif *pcif = j2c(lcif, ffi_cif);                 \
    void (*pfunction)(void) = FFI_FN(j2vp(jfun));       \
    void ** pavalues = j2c(jav, void*);                 \
    checkNullPointer(env, pcif, ret);                   \
    checkNullPointer(env, pfunction, ret);              \
    checkNullPointer(env, pavalues, ret);               \
    ffi_type * rtype = pcif->rtype;                     \
    uint64_t res = 0;                                   \
    void * retAddr = (rtype->size <= sizeof (res))      \
            ? &res : alloca(rtype->size);               \
    ffi_call(pcif, pfunction, retAddr, pavalues);       \
    saveLastError(env, obj, methodId, GetLastError());  \
    RET_##jtype(rtype, retAddr);                        \
}

DEFINE_INVOKE(Void, void, /*void*/);
DEFINE_INVOKE(Boolean, jboolean, 0);
/* DEFINE_INVOKE(Byte, jbyte, 0); */
/* DEFINE_INVOKE(Short, jshort, 0); */
/* DEFINE_INVOKE(Char, jchar, 0); */
DEFINE_INVOKE(Int, jint, 0);
DEFINE_INVOKE(Long, jlong, 0);
DEFINE_INVOKE(Float, jfloat, 0);
DEFINE_INVOKE(Double, jdouble, 0);

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getMethodId
 * Signature: (Ljava/lang/reflect/Method;)J
 */
JNIEXPORT jlong JNICALL Java_jnc_foreign_internal_NativeMethods_getMethodId
(JNIEnv *env, jobject UNUSED(self), jobject jmethod) {
    checkNullPointer(env, jmethod, 0);
    return p2j(CALLJNI(env, FromReflectedMethod, jmethod));
}

#include "jnc.h"
#include "convert.h"

#ifdef _WIN32
#include <windows.h>
#else
#include <errno.h>
#define GetLastError() errno
#endif

static void saveLastError(JNIEnv *env, jobject obj, jlong methodId, int error) {
    jmethodID method = j2p(methodId, jmethodID);
    if (likely(obj != nullptr && method != nullptr)) {
        jvalue v;
        v.i = error;
        env->CallVoidMethodA(obj, method, &v);
    }
}

#if defined(_WIN32) && !defined(_WIN64)
#define GET_ABI(x) ((x == JNC_CALL(STDCALL)) ? FFI_STDCALL : FFI_DEFAULT_ABI)
#else
#define GET_ABI(x) ((x & 0) | FFI_DEFAULT_ABI)
#endif

static void checkReturnValue(JNIEnv *env, ffi_status status) {
    switch (status) {
        case FFI_OK:
            break;
        case FFI_BAD_TYPEDEF:
            throwByName(env, IllegalArgument, "Bad typedef");
            break;
        case FFI_BAD_ABI:
            throwByName(env, IllegalArgument, "Bad abi");
            break;
        default:
            throwByName(env, UnknownError, nullptr);
            break;
    }
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    prepareInvoke
 * Signature: (JIIJJ)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_prepareInvoke
(JNIEnv *env, jobject UNUSED(self), jlong lcif, jint abi, jint nargs,
        jlong lrtype, jlong latype) {
    ffi_cif *pcif = j2c(lcif, ffi_cif);
    ffi_type *prtype = j2c(lrtype, ffi_type);
    ffi_type **patype = j2c(latype, ffi_type*);
    checkNullPointer(env, pcif, /*void*/);
    checkNullPointer(env, prtype, /*void*/);
    checkNullPointer(env, patype, /*void*/);
    checkReturnValue(env, ffi_prep_cif(pcif, (ffi_abi) GET_ABI(abi), (unsigned) nargs, prtype, patype));
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    prepareInvokeVariadic
 * Signature: (JIIIJJ)V
 */
EXTERNC JNIEXPORT void JNICALL Java_jnc_foreign_internal_NativeMethods_prepareInvokeVariadic
(JNIEnv *env, jobject UNUSED(self), jlong lcif, jint abi, jint nfixedargs,
        jint ntotalargs, jlong lrtype, jlong latype) {
    ffi_cif *pcif = j2c(lcif, ffi_cif);
    ffi_type *prtype = j2c(lrtype, ffi_type);
    ffi_type **patype = j2c(latype, ffi_type*);
    checkNullPointer(env, pcif, /*void*/);
    checkNullPointer(env, prtype, /*void*/);
    checkNullPointer(env, patype, /*void*/);
    checkReturnValue(env, ffi_prep_cif_var(pcif, (ffi_abi) GET_ABI(abi),
            (unsigned) nfixedargs, (unsigned) ntotalargs, prtype, patype));
}

#define DEFINE_INVOKE(name, jtype, ret)                             \
EXTERNC JNIEXPORT jtype JNICALL                                     \
Java_jnc_foreign_internal_NativeMethods_invoke##name                \
(JNIEnv * env, jobject UNUSED(self), jlong lcif, jlong jfun,        \
        jlong base, jintArray offsets, jobject obj,                 \
        jlong methodId) {                                           \
    ffi_cif *pcif = j2c(lcif, ffi_cif);                             \
    void (*pfunction)(void) = FFI_FN(j2vp(jfun));                   \
    checkNullPointer(env, pcif, ret);                               \
    checkNullPointer(env, pfunction, ret);                          \
    uint32_t cnt = base && offsets ?                                \
        CALLJNI(env, GetArrayLength, offsets) : 0;                  \
    void ** pavalues;                                               \
    if (likely(cnt != 0)) {                                         \
        auto unit = sizeof (void *) + sizeof (jint);                \
        pavalues = static_cast<void**>(alloca (cnt * unit));        \
        jint* joff = (jint*) (void*) &pavalues[cnt];                \
        CALLJNI(env, GetIntArrayRegion, offsets, 0, cnt, joff);     \
        uint32_t i = 0;                                             \
        for (; i != cnt; ++i) {                                     \
            pavalues[i] = j2c(base + joff[i], void*);               \
        }                                                           \
    } else {                                                        \
        pavalues = nullptr;                                         \
    }                                                               \
    ffi_type * rtype = pcif->rtype;                                 \
    uint64_t res = 0;                                               \
    void * retAddr = likely(rtype->size <= sizeof (res))            \
            ? &res : alloca(rtype->size);                           \
    ffi_call(pcif, pfunction, retAddr, pavalues);                   \
    saveLastError(env, obj, methodId, GetLastError());              \
    RET_##jtype(rtype, retAddr);                                    \
}

DEFINE_INVOKE(Void, void, /*void*/);
DEFINE_INVOKE(Boolean, jboolean, 0);
DEFINE_INVOKE(Int, jint, 0);
DEFINE_INVOKE(Long, jlong, 0);
DEFINE_INVOKE(Double, jdouble, 0);

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getMethodId
 * Signature: (Ljava/lang/reflect/Method;)J
 */
EXTERNC JNIEXPORT jlong JNICALL Java_jnc_foreign_internal_NativeMethods_getMethodId
(JNIEnv *env, jobject UNUSED(self), jobject jmethod) {
    checkNullPointer(env, jmethod, 0);
    return p2j(CALLJNI(env, FromReflectedMethod, jmethod));
}

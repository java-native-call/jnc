#include "jnc.h"

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
 * Class:     jnc_provider_NativeMethods
 * Method:    prepareInvoke
 * Signature: (JIIJJ)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_provider_NativeMethods_prepareInvoke
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
 * Class:     jnc_provider_NativeMethods
 * Method:    prepareInvokeVariadic
 * Signature: (JIIIJJ)V
 */
EXTERNC JNIEXPORT void JNICALL Java_jnc_provider_NativeMethods_prepareInvokeVariadic
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

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    getMethodId
 * Signature: (Ljava/lang/reflect/Method;)J
 */
EXTERNC JNIEXPORT jlong JNICALL Java_jnc_provider_NativeMethods_getMethodId
(JNIEnv *env, jobject UNUSED(self), jobject jmethod) {
    checkNullPointer(env, jmethod, 0);
    return p2j(CALLJNI(env, FromReflectedMethod, jmethod));
}

template <class Dest, class Source>
inline Dest bit_cast(const Source& source) {
    static_assert(sizeof (Dest) == sizeof (Source),
            "bit_cast requires source and destination to be the same size");
    // TODO is_trivially_copyable ??
    Dest dest;
    memcpy(&dest, &source, sizeof (dest));
    return dest;
}

template<class _Tp, bool = jnc_type_traits::is_integral<_Tp>::value>
struct convertor;

template<class _Tp>
struct convertor<_Tp, true> {
    // use reference to avoid type conversion mismatch

    jlong operator()(_Tp &value) const {
        return value;
    }

};

template<>
struct convertor<void, false> {

    jlong operator()() const {
        return 0;
    }

};

template<>
struct convertor<float, false> {

    jlong operator()(float& value) const {
        return bit_cast<jint>(value);
    }

};

template<>
struct convertor<double, false> {

    jlong operator()(double& value) const {
        return bit_cast<jlong>(value);
    }

};

union result_t {
    max_align_t mat;
    jvalue jv;
};

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    invoke
 * Signature: (JJJ[ILjava/lang/Object;J)J
 */
EXTERNC JNIEXPORT jlong JNICALL
Java_jnc_provider_NativeMethods_invoke
(JNIEnv * env, jobject UNUSED(self), jlong lcif, jlong jfun,
        jlong base, jintArray offsets, jobject obj,
        jlong methodId) {
    ffi_cif *pcif = j2c(lcif, ffi_cif);
    void (*pfunction)(void) = FFI_FN(j2vp(jfun));
    checkNullPointer(env, pcif, 0);
    checkNullPointer(env, pfunction, 0);
    auto rtypetype = pcif->rtype->type;
    auto rtypesize = pcif->rtype->size;
    switch (rtypetype) {
        case JNC_TYPE(VOID):
        case JNC_TYPE(FLOAT):
        case JNC_TYPE(DOUBLE):
        case JNC_TYPE(UINT8):
        case JNC_TYPE(SINT8):
        case JNC_TYPE(UINT16):
        case JNC_TYPE(SINT16):
        case JNC_TYPE(UINT32):
        case JNC_TYPE(SINT32):
        case JNC_TYPE(UINT64):
        case JNC_TYPE(SINT64):
        case JNC_TYPE(POINTER):
            break;
        default:
            throwByName(env, IllegalArgument, nullptr);
            return 0;
    }
    uint32_t cnt = base && offsets ?
            CALLJNI(env, GetArrayLength, offsets) : 0;
    void ** pavalues;
    if (likely(cnt != 0)) {
        static_assert(alignof (void *) % alignof (jint) == 0,
                "align of pointer is not multiple of align of int, will got unaligned access");
        auto unit = sizeof (void *) + sizeof (jint);
        pavalues = static_cast<void**> (alloca(cnt * unit));
        jint* joff = (jint*) (void*) &pavalues[cnt];
        CALLJNI(env, GetIntArrayRegion, offsets, 0, cnt, joff);
        for (uint32_t i = 0; i != cnt; ++i) {
            pavalues[i] = j2c(base + joff[i], void*);
        }
    } else {
        pavalues = nullptr;
    }
    result_t result;
    // typeof pcif->rtype->size is size_t, no need to check not less than 0
    if (likely(rtypesize <= sizeof (result))) {
        void * retAddr = &result;
        ffi_call(pcif, pfunction, retAddr, pavalues);
        saveLastError(env, obj, methodId, GetLastError());
        switch (rtypetype) {
            case JNC_TYPE(VOID): return convertor<void>()();
            case JNC_TYPE(FLOAT): return convertor<float>()(*reinterpret_cast<float*> (retAddr));
            case JNC_TYPE(DOUBLE): return convertor<double>()(*reinterpret_cast<double*> (retAddr));
            case JNC_TYPE(UINT8): return convertor<uint8_t>()(*reinterpret_cast<uint8_t*> (retAddr));
            case JNC_TYPE(SINT8): return convertor<int8_t>()(*reinterpret_cast<int8_t*> (retAddr));
            case JNC_TYPE(UINT16): return convertor<uint16_t>()(*reinterpret_cast<uint16_t*> (retAddr));
            case JNC_TYPE(SINT16): return convertor<int16_t>()(*reinterpret_cast<int16_t*> (retAddr));
            case JNC_TYPE(UINT32): return convertor<uint32_t>()(*reinterpret_cast<uint32_t*> (retAddr));
            case JNC_TYPE(SINT32): return convertor<int32_t>()(*reinterpret_cast<int32_t*> (retAddr));
            case JNC_TYPE(UINT64): return convertor<uint64_t>()(*reinterpret_cast<uint64_t*> (retAddr));
            case JNC_TYPE(SINT64): return convertor<int64_t>()(*reinterpret_cast<int64_t*> (retAddr));
            case JNC_TYPE(POINTER): return p2j(*reinterpret_cast<void**> (retAddr));
            default:
                // fall through
                // we have checked the values before
                ;
        }
    }
    throwByName(env, UnknownError, nullptr);
    return 0;
}

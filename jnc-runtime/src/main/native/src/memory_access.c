#include "jnc.h"
#include "convert.h"

static bool checkNullAndRange(JNIEnv *env, jarray array, jint offset, jint size) {
    checkNullPointer(env, array, false);
    if (unlikely(offset < 0 || offset > CALLJNI(env, GetArrayLength, array) - size)) {
        throwByName(env, ArrayIndexOutOfBounds, NULL);
        return false;
    }
    return true;
}

#define ADDRESS_ARRAY_ACCESS(atype, jni, fname)             \
JNIEXPORT void JNICALL                                      \
Java_jnc_foreign_internal_NativeMethods_get##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (likely(checkNullAndRange(env, array, off, len)))    \
        CALLJNI(env, Set##jni##ArrayRegion, array, off,     \
            len, addr);                                     \
}                                                           \
JNIEXPORT void JNICALL                                      \
Java_jnc_foreign_internal_NativeMethods_put##fname          \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,           \
        atype##Array array, jint off, jint len) {           \
    atype * addr = j2c(laddr, atype);                       \
    checkNullPointer(env, addr, /*void*/);                  \
    if (likely(checkNullAndRange(env, array, off, len)))    \
        CALLJNI(env, Get##jni##ArrayRegion,                 \
            array, off, len, addr);                         \
}

ADDRESS_ARRAY_ACCESS(jbyte, Byte, Bytes);
ADDRESS_ARRAY_ACCESS(jshort, Short, ShortArray);
ADDRESS_ARRAY_ACCESS(jchar, Char, CharArray);
ADDRESS_ARRAY_ACCESS(jint, Int, IntArray);
ADDRESS_ARRAY_ACCESS(jlong, Long, LongArray);
ADDRESS_ARRAY_ACCESS(jfloat, Float, FloatArray);
ADDRESS_ARRAY_ACCESS(jdouble, Double, DoubleArray);

#define DEFINE_PUTTER(name, jtype)                  \
JNIEXPORT void JNICALL                              \
Java_jnc_foreign_internal_NativeMethods_put##name   \
(JNIEnv * env, jobject UNUSED(self), jlong laddr,   \
        jlong ltype, jtype value) {                 \
    void * paddr = j2vp(laddr);                     \
    ffi_type *ptype = j2c(ltype, ffi_type);         \
    checkNullPointer(env, paddr, /*void*/);         \
    checkNullPointer(env, ptype, /*void*/);         \
    PUT_ALL(ptype, paddr, value);                   \
}

DEFINE_PUTTER(Int, jint);
DEFINE_PUTTER(Long, jlong);
DEFINE_PUTTER(Float, jfloat);
DEFINE_PUTTER(Double, jdouble);

#define DEFINE_GETTER(name, jtype)                                  \
JNIEXPORT jtype JNICALL                                             \
Java_jnc_foreign_internal_NativeMethods_get##name                   \
(JNIEnv * env, jobject UNUSED(self), jlong laddr, jlong ltype) {    \
    void * paddr = j2vp(laddr);                                     \
    ffi_type * ptype = j2c(ltype, ffi_type);                        \
    checkNullPointer(env, paddr, 0);                                \
    checkNullPointer(env, ptype, 0);                                \
    RET_##jtype(ptype, paddr);                                      \
}

DEFINE_GETTER(Boolean, jboolean);
DEFINE_GETTER(Int, jint);
DEFINE_GETTER(Long, jlong);
DEFINE_GETTER(Float, jfloat);
DEFINE_GETTER(Double, jdouble);

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    putStringUTF
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_putStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jstring value) {
    void *paddr = j2vp(laddr);
    checkNullPointer(env, paddr, /*void*/);
    checkNullPointer(env, value, /*void*/);
    DO_WITH_STRING_UTF(env, value, str, len, memcpy(paddr, str, (size_t) (len + 1)), /*void*/);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTF
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, NULL);
    return CALLJNI(env, NewStringUTF, paddr);
}

#define checkLimit(env, limit, ret) \
do { \
    if (unlikely(limit <= 0)) { \
        if (likely(limit == 0)) { \
            return CALLJNI(env, NewStringUTF, ""); \
        } \
        throwByName(env, IllegalArgument, "limit>=0"); \
        return ret; \
    } \
} while(false)

#if SIZE_MAX == UINT64_MAX
#define LP64_ONLY(x) x
#define NOT_LP64(x)
#elif SIZE_MAX == UINT32_MAX
#define LP64_ONLY(x)
#define NOT_LP64(x) x
#else
#error unknown type size_t
#endif

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTFN
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTFN
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    checkLimit(env, limit, NULL);
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, NULL);
    LP64_ONLY(size_t szLimit = (size_t) limit);
    NOT_LP64(size_t szLimit = (size_t) MIN(limit, (jlong) (UINT32_MAX - 1)));
    if (likely(NULL != memchr(paddr, 0, szLimit))) {
        return CALLJNI(env, NewStringUTF, paddr);
    }
    char *tmp = (char *) malloc(szLimit + 1);
    checkOutOfMemory(env, tmp, NULL);
    memcpy(tmp, paddr, szLimit);
    tmp[szLimit] = 0;
    jstring result = CALLJNI(env, NewStringUTF, tmp);
    free(tmp);
    return result;
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    putStringChar16
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_jnc_foreign_internal_NativeMethods_putStringChar16
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jstring value) {
    void *paddr = j2vp(laddr);
    checkNullPointer(env, paddr, /*void*/);
    checkNullPointer(env, value, /*void*/);
    DO_WITH_STRING_16(env, value, str, len, memcpy(paddr, str, (size_t) (len + 1) * sizeof (jchar)), /*void*/);
}

static jstring returnNewString(JNIEnv *env, const jchar *addr, size_t len) {
    // parameter to call NewString is jsize, which is alias of jint
    if (unlikely(len > INT32_MAX)) {
        // can't find a presentation for this length
        throwByName(env, OutOfMemory, NULL);
        return NULL;
    }
    return CALLJNI(env, NewString, addr, len);
}

static jstring copyNewString(JNIEnv *env, const jchar *addr, size_t len) {
    if (unlikely(len > INT32_MAX)) {
        // can't find a presentation for this length
        throwByName(env, OutOfMemory, NULL);
        return NULL;
    }
    jchar *tmp = (jchar *) malloc(len * sizeof (jchar));
    checkOutOfMemory(env, tmp, NULL);
    memcpy(tmp, addr, len * sizeof (jchar));
    jstring res = CALLJNI(env, NewString, tmp, len);
    free(tmp);
    return res;
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringChar16
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jnc_foreign_internal_NativeMethods_getStringChar16
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    const jchar *paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, NULL);
    if (unlikely(laddr & 1)) {
        // unaligned access
        const jchar *p = j2c(laddr + 1, jchar), *q = p;
        // require c99 if defined in for loop
        uint16_t x = *j2c(laddr, uint8_t), y;
        for (;; x = y, ++p) {
            y = *p;
            x = (x << 8) | (y >> 8);
            if (x == 0) break;
        }
        return copyNewString(env, paddr, p - q);
    }
#if WCHAR_MAX == UINT16_MAX
    // on windows
    size_t len = wcslen((wchar_t*) paddr);
#else
    const jchar *p = paddr;
    while (*p) ++p;
    size_t len = p - paddr;
#endif
    return returnNewString(env, paddr, len);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringChar16N
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jnc_foreign_internal_NativeMethods_getStringChar16N
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    checkLimit(env, limit, NULL);
    const jchar *paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, NULL);
    LP64_ONLY(size_t szLimit = (size_t) limit);
    NOT_LP64(size_t szLimit = (size_t) MIN(limit, (jlong) (UINT32_MAX - 1)));
    szLimit /= sizeof (jchar);
    if (unlikely(laddr & 1)) {
        // unaligned access
        const jchar *p = j2c(laddr + 1, jchar), *q = p;
        // require c99 if defined in for loop
        uint16_t x = *j2c(laddr, uint8_t), y;
        for (;; x = y, ++p) {
            y = *p;
            x = (x << 8) | (y >> 8);
            if (szLimit-- == 0 || x == 0) break;
        }
        return copyNewString(env, paddr, p - q);
    }
    const jchar *p = paddr;
    while (szLimit-- > 0 && *p) ++p;
    size_t len = p - paddr;
    return returnNewString(env, paddr, len);
}

#define ADDRESS_ACCESS_E(name, native, jtype, j2n, n2j)             \
JNIEXPORT void JNICALL                                              \
Java_jnc_foreign_internal_NativeMethods_putRaw##name                \
(JNIEnv *env, jclass UNUSED(class), jlong address, jtype value) {   \
    native * paddr = j2c(address, native);                          \
    checkNullPointer(env, paddr, /*void*/);                         \
    *paddr = j2n(value);                                            \
}                                                                   \
JNIEXPORT jtype JNICALL                                             \
Java_jnc_foreign_internal_NativeMethods_getRaw##name                \
(JNIEnv *env, jclass UNUSED(class), jlong address) {                \
    native * paddr = j2c(address, native);                          \
    checkNullPointer(env, paddr, 0);                                \
    return n2j(*paddr);                                             \
}

#define ADDRESS_ACCESS(name, jtype) \
    ADDRESS_ACCESS_E(name, jtype, jtype, NOOP, NOOP)
ADDRESS_ACCESS(Byte, jbyte);
ADDRESS_ACCESS(Short, jshort);
ADDRESS_ACCESS(Char, jchar);
ADDRESS_ACCESS(Int, jint);
ADDRESS_ACCESS(Long, jlong);
ADDRESS_ACCESS(Float, jfloat);
ADDRESS_ACCESS(Double, jdouble);
ADDRESS_ACCESS_E(Address, void*, jlong, j2vp, p2j);

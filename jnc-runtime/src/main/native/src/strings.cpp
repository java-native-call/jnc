#include "jnc.h"

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    putStringUTF
 * Signature: (JLjava/lang/String;)V
 */
EXTERNC JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_putStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jstring value) {
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, /*void*/);
    checkNullPointer(env, value, /*void*/);
    jsize utfLen = CALLJNI(env, GetStringUTFLength, value);
    jsize len = CALLJNI(env, GetStringLength, value);
    if (unlikely(CALLJNI(env, ExceptionCheck))) return;
    // It is said that some jvm implementation
    // will not got terminated character
    paddr[utfLen] = 0;
    CALLJNI(env, GetStringUTFRegion, value, 0, len, paddr);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTF
 * Signature: (J)Ljava/lang/String;
 */
EXTERNC JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, nullptr);
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

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTFN
 * Signature: (JJ)Ljava/lang/String;
 */
EXTERNC JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTFN
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    checkLimit(env, limit, nullptr);
    char *paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, nullptr);
    LP64_ONLY(size_t szLimit = (size_t) limit);
    NOT_LP64(size_t szLimit = (size_t) MIN(limit, (jlong) (UINT32_MAX - 1)));
    if (likely(nullptr != memchr(paddr, 0, szLimit))) {
        return CALLJNI(env, NewStringUTF, paddr);
    }
    char *tmp = (char *) malloc(szLimit + 1);
    checkOutOfMemory(env, tmp, nullptr);
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
EXTERNC JNIEXPORT void JNICALL Java_jnc_foreign_internal_NativeMethods_putStringChar16
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jstring value) {
    jchar *paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, /*void*/);
    checkNullPointer(env, value, /*void*/);
    jsize len = CALLJNI(env, GetStringLength, value);
    if (unlikely(CALLJNI(env, ExceptionCheck))) return;
    paddr[len] = 0;
    CALLJNI(env, GetStringRegion, value, 0, len, paddr);
}

static jstring returnNewString(JNIEnv *env, const jchar *addr, size_t len) {
    // parameter to call NewString is jsize, which is alias of jint
    if (unlikely(len > INT32_MAX)) {
        // can't find a presentation for this length
        throwByName(env, OutOfMemory, nullptr);
        return nullptr;
    }
    return CALLJNI(env, NewString, addr, len);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringChar16
 * Signature: (J)Ljava/lang/String;
 */
EXTERNC JNIEXPORT jstring JNICALL Java_jnc_foreign_internal_NativeMethods_getStringChar16
(JNIEnv *env, jobject UNUSED(self), jlong laddr) {
    const jchar *paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, nullptr);
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
EXTERNC JNIEXPORT jstring JNICALL Java_jnc_foreign_internal_NativeMethods_getStringChar16N
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    checkLimit(env, limit, nullptr);
    const jchar *paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, nullptr);
    LP64_ONLY(size_t szLimit = (size_t) limit);
    NOT_LP64(size_t szLimit = (size_t) MIN(limit, (jlong) (UINT32_MAX - 1)));
    szLimit /= sizeof (jchar);
    const jchar *p = paddr;
    while (szLimit-- > 0 && *p) ++p;
    size_t len = p - paddr;
    return returnNewString(env, paddr, len);
}

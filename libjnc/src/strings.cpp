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
    jsize utfLen = env->GetStringUTFLength(value);
    jsize len = env->GetStringLength(value);
    if (unlikely(env->ExceptionCheck())) return;
    // It is said that some jvm implementation
    // will not got terminated character
    paddr[utfLen] = 0;
    env->GetStringUTFRegion(value, 0, len, paddr);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTFLength
 * Signature: (Ljava/lang/String;)I
 */
EXTERNC JNIEXPORT jint JNICALL Java_jnc_foreign_internal_NativeMethods_getStringUTFLength
(JNIEnv *env, jobject UNUSED(self), jstring value) {
    checkNullPointer(env, value, 0);
    return env->GetStringUTFLength(value);
}

static size_t get_string_length_2(const jchar * const addr, jlong limit) noexcept {
    if (limit == -1) {
#if WCHAR_MAX == UINT16_MAX
        // on windows
        return wcslen((wchar_t*) addr);
#else
        const jchar *p = addr;
        while (*p) ++p;
        return p - addr;
#endif
    } else {
        size_t szLimit;
        LP64_ONLY(szLimit = size_t(limit));
        NOT_LP64(szLimit = size_t(MIN(limit, (jlong) (SIZE_MAX - 1))));
        szLimit /= sizeof (jchar);
        const jchar *p = addr;
        while (szLimit-- > 0 && *p) ++p;
        return p - addr;
    }
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringUTF
 * Signature: (JJ)Ljava/lang/String;
 */
EXTERNC JNIEXPORT jstring JNICALL
Java_jnc_foreign_internal_NativeMethods_getStringUTF
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    const char *const paddr = j2c(laddr, char);
    checkNullPointer(env, paddr, nullptr);
    if (limit == 0) return env->NewStringUTF("");
    if (limit == -1) return env->NewStringUTF(paddr);
    if (unlikely(limit < -1)) {
        throwByName(env, IllegalArgument, nullptr);
        return 0;
    }
    size_t szLimit;
    LP64_ONLY(szLimit = size_t(limit));
    NOT_LP64(szLimit = size_t(min(limit, (jlong) (SIZE_MAX - 1))));
    const char *p = paddr;
    for (size_t n = szLimit; n; --n) {
        if (!*p++) return env->NewStringUTF(paddr);
    }
    char *tmp = (char *) malloc(szLimit + 1);
    checkOutOfMemory(env, tmp, nullptr);
    memcpy(tmp, paddr, szLimit);
    tmp[szLimit] = 0;
    jstring result = env->NewStringUTF(tmp);
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
    jchar * const paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, /*void*/);
    checkNullPointer(env, value, /*void*/);
    jsize len = env->GetStringLength(value);
    if (unlikely(env->ExceptionCheck())) return;
    paddr[len] = 0;
    env->GetStringRegion(value, 0, len, paddr);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringChar16
 * Signature: (JJ)Ljava/lang/String;
 */
EXTERNC JNIEXPORT jstring JNICALL Java_jnc_foreign_internal_NativeMethods_getStringChar16
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit) {
    const jchar * const paddr = j2c(laddr, jchar);
    checkNullPointer(env, paddr, nullptr);
    if (limit == 0) return env->NewStringUTF("");
    if (unlikely(limit < -1)) {
        throwByName(env, IllegalArgument, nullptr);
        return nullptr;
    }
    size_t len = get_string_length_2(paddr, limit);
    // parameter to call NewString is jsize, which is alias of jint
    if (unlikely(len > INT32_MAX)) {
        // can't find a presentation for this length
        throwByName(env, OutOfMemory, nullptr);
        return nullptr;
    }
    return env->NewString(paddr, len);
}

static size_t get_string_length_1(const char *const addr, jlong limit) {
    if (limit == -1) {
        return strlen(addr);
    } else {
        size_t szLimit;
        LP64_ONLY(szLimit = size_t(limit));
        NOT_LP64(szLimit = size_t(MIN(limit, (jlong) (SIZE_MAX - 1))));
        // Behavior of `memchr` is not defined if not found in searching range.
        const char *p = addr;
        while (szLimit-- > 0 && *p) ++p;
        return p - addr;
    }
}

static size_t get_string_length_4(const jint * const addr, jlong limit) {
    if (limit == -1) {
#if WCHAR_MAX == UINT32_MAX
        return wcslen(reinterpret_cast<wchar_t*> (addr));
#else
        // on Windows
        const jint *p = addr;
        while (*p) ++p;
        return p - addr;
#endif
    } else {
        size_t szLimit;
        LP64_ONLY(szLimit = size_t(limit));
        NOT_LP64(szLimit = size_t(MIN(limit, (jlong) (SIZE_MAX - 1))));
        szLimit /= sizeof (jint);
        const jint *p = addr;
        while (szLimit-- > 0 && *p) ++p;
        return p - addr;
    }
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getStringLength
 * Signature: (JJI)I
 */
EXTERNC JNIEXPORT jint JNICALL Java_jnc_foreign_internal_NativeMethods_getStringLength
(JNIEnv *env, jobject UNUSED(self), jlong laddr, jlong limit, jint terminatorLength) {
    const void *const paddr = j2vp(laddr);
    checkNullPointer(env, paddr, 0);
    if (unlikely(limit < -1)) goto iae;
    size_t len;
    switch (terminatorLength) {
        case 1:
            len = get_string_length_1(reinterpret_cast<const char*> (paddr), limit);
            break;
        case 2:
            len = get_string_length_2(reinterpret_cast<const jchar*> (paddr), limit);
            break;
        case 4:
            len = get_string_length_4(reinterpret_cast<const jint*> (paddr), limit);
            break;
        default:
            goto iae;
    }
    if (len > INT32_MAX) return INT32_MAX;
    return (jint) len;
iae:
    throwByName(env, IllegalArgument, nullptr);
    return 0;
}

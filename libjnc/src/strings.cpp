#include "jnc.h"

template<int = sizeof (size_t)>
struct shift_selector;

template<>
struct shift_selector<4> : jnc_type_traits::integral_constant<int, 30> {
};

template<>
struct shift_selector<8> : jnc_type_traits::integral_constant<int, 62> {
};

/* limit must not be negative, should be checked before pass to this function */
inline bool is_unlimited(jlong limit) {
    static_assert(sizeof(void *) == sizeof(size_t), "require pointer and size_t same size");
    // maybe the limit here we got is calculated by minus something.
    // treat it as unlimited if the limit is greater than a quart of the whole memory can be presented.
    return limit >> shift_selector<>::value;
}

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
    env->GetStringUTFRegion(value, 0, len, paddr);
    if (unlikely(env->ExceptionCheck())) return;
    paddr[utfLen] = 0;
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
    // limit is non negative
    if (is_unlimited(limit)) {
#if WCHAR_MAX == UINT16_MAX
        // on windows
        return wcslen((wchar_t*) addr);
#else
        const jchar *p = addr;
        while (*p) ++p;
        return p - addr;
#endif
    } else {
        size_t szLimit = limit / sizeof (jchar);
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
    if (unlikely(limit < 0)) {
        throwByName(env, IllegalArgument, nullptr);
        return 0;
    }
    if (is_unlimited(limit)) return env->NewStringUTF(paddr);
    size_t szLimit = limit;
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
    env->GetStringRegion(value, 0, len, paddr);
    if (unlikely(env->ExceptionCheck())) return;
    paddr[len] = 0;
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
    if (unlikely(limit < 0)) {
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
    if (is_unlimited(limit)) {
        return strlen(addr);
    } else {
        size_t szLimit = limit;
        // Behavior of `memchr` is not defined if not found in searching range.
        const char *p = addr;
        while (szLimit-- > 0 && *p) ++p;
        return p - addr;
    }
}

static size_t get_string_length_4(const jint * const addr, jlong limit) {
    if (is_unlimited(limit)) {
#if WCHAR_MAX == UINT32_MAX
        return wcslen(reinterpret_cast<wchar_t*> (addr));
#else
        // on Windows
        const jint *p = addr;
        while (*p) ++p;
        return p - addr;
#endif
    } else {
        size_t szLimit = limit / sizeof (jint);
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
    if (unlikely(limit < 0)) goto iae;
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
    if (unlikely(len > INT32_MAX)) return INT32_MAX;
    return (jint) len;
iae:
    throwByName(env, IllegalArgument, nullptr);
    return 0;
}

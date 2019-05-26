#include "jnc.h"

#ifdef _WIN32
#include <windows.h>
#define RTLD_LAZY 0
/* and zero to avoid unused parameter */
#define JNC2RTLD(x) ((x) & 0)
#define dlopen(path, mode) (path ? LoadLibraryExW(path, NULL, mode) : GetModuleHandleW(NULL))
#define dlsym(hModule, symbol) GetProcAddress(hModule, symbol)
#define dlclose(module) !FreeLibrary(module)
/* assume wchar_t on windows is 2 byte, compile error when not */
#if WCHAR_MAX != UINT16_MAX
#error Unsupported wchar_t type
#else /* WCHAR_MAX != UINT16_MAX */
#define DO_WITH_PLATFORM_STRING DO_WITH_STRING_16
#define DLOPEN_PARAM_TYPE LPWSTR
#endif /* WCHAR_MAX != UINT16_MAX */

#define SIG_STRING "L" JAVA_LANG_STR(String) ";"

#define throwByNameA(key, sig, env, name, value)                            \
do {                                                                        \
    jclass jc_ = CALLJNI(env, FindClass, name);                             \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break;                      \
    jmethodID _jm = CALLJNI(env, GetMethodID, jc_, "<init>", "(" sig ")V"); \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break;                      \
    jvalue jv_;                                                             \
    jv_.key = value;                                                        \
    jobject jo_ = CALLJNI(env, NewObjectA, jc_, _jm, &jv_);                 \
    if (unlikely(CALLJNI(env, ExceptionCheck))) break;                      \
    CALLJNI(env, Throw, jo_);                                               \
    CALLJNI(env, DeleteLocalRef, jo_);                                      \
    CALLJNI(env, DeleteLocalRef, jc_);                                      \
} while(false)
#define throwByNameS(...) throwByNameA(l, SIG_STRING, __VA_ARGS__)

static void throwByLastError(JNIEnv * env, const char * type) {
    DWORD dw = GetLastError();
    LPWSTR lpMsgBuf = NULL;
    if (unlikely(!FormatMessageW(
            FORMAT_MESSAGE_ALLOCATE_BUFFER |
            FORMAT_MESSAGE_FROM_SYSTEM |
            FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            dw,
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            (LPWSTR) & lpMsgBuf,
            0, NULL))) {
        throwByName(env, OutOfMemory, NULL);
        return;
    }
    // trust system call return value
    // assume lpMsgBuf is not NULL
    size_t len = wcslen(lpMsgBuf);
    if (likely(len > 0 && lpMsgBuf[len - 1] == '\n'))--len;
    if (likely(len > 0 && lpMsgBuf[len - 1] == '\r'))--len;
    jstring string = CALLJNI(env, NewString, (jchar*) lpMsgBuf, len);
    LocalFree(lpMsgBuf);
    if (unlikely(CALLJNI(env, ExceptionCheck))) return;
    throwByNameS(env, type, string);
}

#else /* _WIN32 */

#include <dlfcn.h>

#define JNC_RTLD(name)  jnc_foreign_internal_NativeMethods_RTLD_##name
#define RTLD(name)      RTLD_##name
#define DEFAULT_RTLD    (RTLD(LAZY) | RTLD(LOCAL))
#define JNC2RTLD(x)                             \
(x) ? (                                         \
((x) & JNC_RTLD(LAZY)   ? RTLD(LAZY) : 0)   |   \
((x) & JNC_RTLD(NOW)    ? RTLD(NOW) : 0)    |   \
((x) & JNC_RTLD(LOCAL)  ? RTLD(LOCAL) : 0)  |   \
((x) & JNC_RTLD(GLOBAL) ? RTLD(GLOBAL) : 0)     \
) : DEFAULT_RTLD

#define HMODULE void*
#define DO_WITH_PLATFORM_STRING DO_WITH_STRING_UTF
#define DLOPEN_PARAM_TYPE char*
#define throwByLastError(env, type)         \
do {                                        \
    const char * _msg = dlerror();          \
    if (!_msg) _msg = "unknown dl-error";   \
    throwByName(env, type, _msg);           \
} while(false)

#ifndef RTLD_NOW
#define RTLD_NOW 0
#endif /* RTLD_NOW */

#ifndef RTLD_LAZY
#define RTLD_LAZY 1
#endif /* RTLD_LAZY */

#endif /* _WIN32 */

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    dlopen
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_dlopen
(JNIEnv *env, jobject UNUSED(self), jstring path, jint mode) {
    HMODULE ret = NULL;
    if (unlikely(NULL == path)) {
#ifdef __BIONIC__
        ret = RTLD_DEFAULT;
#else
        ret = dlopen(NULL, RTLD_LAZY);
#endif
    } else {
        DO_WITH_PLATFORM_STRING(env, path, buf, len, ret = dlopen((DLOPEN_PARAM_TYPE) (void*) buf, JNC2RTLD(mode)), 0);
    }
    if (unlikely(NULL == ret)) {
        throwByLastError(env, UnsatisfiedLink);
    }
    return p2j(ret);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    dlsym
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_jnc_foreign_internal_NativeMethods_dlsym
(JNIEnv *env, jobject UNUSED(self), jlong lhandle, jstring symbol) {
    HMODULE hModule = j2p(lhandle, HMODULE);
    checkNullPointer(env, hModule, 0);
    checkNullPointer(env, symbol, 0);
    jlong ret = 0;
    DO_WITH_STRING_UTF(env, symbol, psymbol, len, ret = p2j(dlsym(hModule, psymbol)), 0);
    if (unlikely(ret == 0)) {
        throwByLastError(env, UnsatisfiedLink);
    }
    return ret;
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    dlclose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_jnc_foreign_internal_NativeMethods_dlclose
(JNIEnv *env, jobject UNUSED(self), jlong lhandle) {
    HMODULE hModule = j2p(lhandle, HMODULE);
    checkNullPointer(env, hModule, /*void*/);
#ifdef _WIN32
    if (unlikely(GetModuleHandleW(NULL) == (hModule))) return;
#elif defined(__BIONIC__)
    if (hModule == RTLD_DEFAULT) return;
#endif
    if (unlikely(dlclose(hModule))) {
        throwByLastError(env, UnknownError);
    }
}

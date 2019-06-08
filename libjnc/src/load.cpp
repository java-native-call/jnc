#include "jnc.h"

#ifdef _WIN32
#include <windows.h>
#else /* _WIN32 */

#include <unistd.h>
#endif
static jint _page_size;

EXTERNC JNIEXPORT jint JNICALL JNI_OnLoad
(JavaVM *UNUSED(vm), void *UNUSED(reserved)) {
#ifdef _WIN32
    SYSTEM_INFO si;
    GetSystemInfo(&si);
    _page_size = si.dwPageSize;
#else
    _page_size = sysconf(_SC_PAGESIZE);
#endif
    return JNI_VERSION_1_6;
}

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    getJniVersion
 * Signature: ()I
 */
EXTERNC JNIEXPORT jint JNICALL
Java_jnc_provider_NativeMethods_getJniVersion
(JNIEnv *env, jobject UNUSED(self)) {
    return env->GetVersion();
}

/*
 * Class:     jnc_provider_NativeMethods
 * Method:    pageSize
 * Signature: ()I
 */
EXTERNC JNIEXPORT jint JNICALL
Java_jnc_provider_NativeMethods_pageSize
(JNIEnv *UNUSED(env), jobject UNUSED(self)) {
    return _page_size;
}

EXTERNC JNIEXPORT void JNICALL JNI_OnUnload
(JavaVM *vm, void *UNUSED(reserved)) {
    JNIEnv *env;
    if (likely(vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK)) {
        jclass type = env->FindClass("jnc/foreign/internal/NativeMethods");
        if (unlikely(type == nullptr)) {
            env->ExceptionClear();
            return;
        }
        jmethodID methodId = env->GetStaticMethodID(type, "onUnload", "()V");
        if (unlikely(methodId == nullptr)) {
            env->ExceptionClear();
            return;
        }
        env->CallStaticVoidMethodA(type, methodId, nullptr);
    }
}

#include "jnc.h"

#ifdef _WIN32
#include <windows.h>
#else /* _WIN32 */

#include <unistd.h>
#endif
static jint _page_size;

JNIEXPORT jint JNICALL JNI_OnLoad
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
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    getJniVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_getJniVersion
(JNIEnv *env, jobject UNUSED(self)) {
    return (*env)->GetVersion(env);
}

/*
 * Class:     jnc_foreign_internal_NativeMethods
 * Method:    pageSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_jnc_foreign_internal_NativeMethods_pageSize
(JNIEnv *UNUSED(env), jobject UNUSED(self)) {
    return _page_size;
}

JNIEXPORT void JNICALL JNI_OnUnload
(JavaVM *vm, void *UNUSED(reserved)) {
    JNIEnv *env;
    if (likely((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) == JNI_OK)) {
        jclass class = (*env)->FindClass(env, "jnc/foreign/internal/NativeMethods");
        if (likely(class != NULL)) {
            jmethodID methodId = (*env)->GetStaticMethodID(env, class, "onUnload", "()V");
            if (likely(methodId != NULL)) {
                (*env)->CallStaticVoidMethodA(env, class, methodId, NULL);
            }
            (*env)->DeleteLocalRef(env, class);
        }
    }
}

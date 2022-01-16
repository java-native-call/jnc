#include "jnc.h"

EXTERNC JNIEXPORT jint JNICALL JNI_OnLoad
(JavaVM *UNUSED(vm), void *UNUSED(reserved)) {
    return JNI_VERSION_1_6;
}

EXTERNC JNIEXPORT void JNICALL JNI_OnUnload
(JavaVM *vm, void *UNUSED(reserved)) {
    JNIEnv *env;
    if (likely(vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK)) {
        jclass type = env->FindClass(ON_UNLOAD_CLASS);
        if (type != nullptr) {
            jmethodID methodId = env->GetStaticMethodID(type, "onUnload", "()V");
            if (methodId != nullptr) {
                env->CallStaticVoidMethodA(type, methodId, nullptr);
            }
        }
    }
}

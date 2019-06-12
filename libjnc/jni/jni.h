/*
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * We used part of Netscape's Java Runtime Interface (JRI) as the starting
 * point of our design and implementation.
 */

/******************************************************************************
 * Java Runtime Interface
 * Copyright (c) 1996 Netscape Communications Corporation. All rights reserved.
 *****************************************************************************/

#ifndef _JAVASOFT_JNI_H_
#define _JAVASOFT_JNI_H_

#include <stdio.h>
#include <stdarg.h>

/* jni_md.h contains the machine-dependent typedefs for jbyte, jint
   and jlong */

#ifndef _JAVASOFT_JNI_MD_H_
#define _JAVASOFT_JNI_MD_H_

#ifdef _WIN32
#define JNIEXPORT __declspec(dllexport)
#define JNIIMPORT __declspec(dllimport)
#define JNICALL __stdcall

typedef long jint;
typedef __int64 jlong;
#else /* _WIN32 */

#if defined(__GNUC__) && (__GNUC__ > 4 || __GNUC__ == 4 && __GNUC_MINOR__ > 2)
#define JNIEXPORT __attribute__((visibility("default")))
#elif defined(__has_attribute) /* !__GNUC__ || __GNUC__ <= 4.2 */
#if __has_attribute(visibility)
#define JNIEXPORT __attribute__((visibility("default")))
#else /* __has_attribute(visibility) */
#define JNIEXPORT
#endif /* __has_attribute(visibility) */
#else /* !__GNUC__ && !defined(__has_attribute) */
#define JNIEXPORT
#endif /* !__GNUC__ && !defined(__has_attribute) */

#define JNIIMPORT JNIEXPORT
#define JNICALL

typedef int jint;
#if defined(__LP64__) && __LP64__ /* for -Wundef */
typedef long jlong;
#else
typedef long long jlong;
#endif

#endif /* _WIN32 */

#if !defined(JNI_FORCEINLINE)
#  if defined(_MSC_VER)
#    define JNI_FORCEINLINE __forceinline
#  elif defined(__GNUC__) && __GNUC__ > 3
/* Clang also defines __GNUC__ (as 4) */
#    define JNI_FORCEINLINE inline __attribute__ ((__always_inline__))
#  else
#    define JNI_FORCEINLINE inline
#  endif
#endif

typedef signed char jbyte;

#endif /* _JAVASOFT_JNI_MD_H_ */

#ifdef __cplusplus
extern "C" {
#endif

/*
 * JNI Types
 */

#ifndef JNI_TYPES_ALREADY_DEFINED_IN_JNI_MD_H

typedef unsigned char jboolean;
typedef unsigned short jchar;
typedef short jshort;
typedef float jfloat;
typedef double jdouble;

typedef jint jsize;

#ifdef __cplusplus

class _jobject {};
class _jclass : public _jobject {};
class _jthrowable : public _jobject {};
class _jstring : public _jobject {};
class _jarray : public _jobject {};
class _jbooleanArray : public _jarray {};
class _jbyteArray : public _jarray {};
class _jcharArray : public _jarray {};
class _jshortArray : public _jarray {};
class _jintArray : public _jarray {};
class _jlongArray : public _jarray {};
class _jfloatArray : public _jarray {};
class _jdoubleArray : public _jarray {};
class _jobjectArray : public _jarray {};

typedef _jobject *jobject;
typedef _jclass *jclass;
typedef _jthrowable *jthrowable;
typedef _jstring *jstring;
typedef _jarray *jarray;
typedef _jbooleanArray *jbooleanArray;
typedef _jbyteArray *jbyteArray;
typedef _jcharArray *jcharArray;
typedef _jshortArray *jshortArray;
typedef _jintArray *jintArray;
typedef _jlongArray *jlongArray;
typedef _jfloatArray *jfloatArray;
typedef _jdoubleArray *jdoubleArray;
typedef _jobjectArray *jobjectArray;

#else

struct _jobject;

typedef struct _jobject *jobject;
typedef jobject jclass;
typedef jobject jthrowable;
typedef jobject jstring;
typedef jobject jarray;
typedef jarray jbooleanArray;
typedef jarray jbyteArray;
typedef jarray jcharArray;
typedef jarray jshortArray;
typedef jarray jintArray;
typedef jarray jlongArray;
typedef jarray jfloatArray;
typedef jarray jdoubleArray;
typedef jarray jobjectArray;

#endif

typedef jobject jweak;

typedef union jvalue {
    jboolean z;
    jbyte b;
    jchar c;
    jshort s;
    jint i;
    jlong j;
    jfloat f;
    jdouble d;
    jobject l;
} jvalue;

struct _jfieldID;
typedef struct _jfieldID *jfieldID;

struct _jmethodID;
typedef struct _jmethodID *jmethodID;

/* Return values from jobjectRefType */
typedef enum _jobjectType {
    JNIInvalidRefType = 0,
    JNILocalRefType = 1,
    JNIGlobalRefType = 2,
    JNIWeakGlobalRefType = 3
} jobjectRefType;


#endif /* JNI_TYPES_ALREADY_DEFINED_IN_JNI_MD_H */

/*
 * jboolean constants
 */

#define JNI_FALSE 0
#define JNI_TRUE 1

/*
 * possible return values for JNI functions.
 */

#define JNI_OK           0                 /* success */
#define JNI_ERR          (-1)              /* unknown error */
#define JNI_EDETACHED    (-2)              /* thread detached from the VM */
#define JNI_EVERSION     (-3)              /* JNI version error */
#define JNI_ENOMEM       (-4)              /* not enough memory */
#define JNI_EEXIST       (-5)              /* VM already created */
#define JNI_EINVAL       (-6)              /* invalid arguments */

/*
 * used in ReleaseScalarArrayElements
 */

#define JNI_COMMIT 1
#define JNI_ABORT 2

/*
 * used in RegisterNatives to describe native method name, signature,
 * and function pointer.
 */

typedef struct {
    char *name;
    char *signature;
    void *fnPtr;
} JNINativeMethod;

/*
 * JNI Native Method Interface.
 */

struct JNINativeInterface_;

struct JNIEnv_;

#ifdef __cplusplus
typedef JNIEnv_ JNIEnv;
#else
typedef const struct JNINativeInterface_ *JNIEnv;
#endif

/*
 * JNI Invocation Interface.
 */

struct JNIInvokeInterface_;

struct JavaVM_;

#ifdef __cplusplus
typedef JavaVM_ JavaVM;
#else
typedef const struct JNIInvokeInterface_ *JavaVM;
#endif

struct JNINativeInterface_ {
    void *reserved0;
    void *reserved1;
    void *reserved2;

    void *reserved3;
    jint (JNICALL *GetVersion)(JNIEnv *env);

    jclass (JNICALL *DefineClass)
            (JNIEnv *env, const char *name, jobject loader, const jbyte *buf,
             jsize len);
    jclass (JNICALL *FindClass)
            (JNIEnv *env, const char *name);

    jmethodID (JNICALL *FromReflectedMethod)
            (JNIEnv *env, jobject method);
    jfieldID (JNICALL *FromReflectedField)
            (JNIEnv *env, jobject field);

    jobject (JNICALL *ToReflectedMethod)
            (JNIEnv *env, jclass cls, jmethodID methodID, jboolean isStatic);

    jclass (JNICALL *GetSuperclass)
            (JNIEnv *env, jclass sub);
    jboolean (JNICALL *IsAssignableFrom)
            (JNIEnv *env, jclass sub, jclass sup);

    jobject (JNICALL *ToReflectedField)
            (JNIEnv *env, jclass cls, jfieldID fieldID, jboolean isStatic);

    jint (JNICALL *Throw)
            (JNIEnv *env, jthrowable obj);
    jint (JNICALL *ThrowNew)
            (JNIEnv *env, jclass clazz, const char *msg);
    jthrowable (JNICALL *ExceptionOccurred)
            (JNIEnv *env);
    void (JNICALL *ExceptionDescribe)
            (JNIEnv *env);
    void (JNICALL *ExceptionClear)
            (JNIEnv *env);
    void (JNICALL *FatalError)
            (JNIEnv *env, const char *msg);

    jint (JNICALL *PushLocalFrame)
            (JNIEnv *env, jint capacity);
    jobject (JNICALL *PopLocalFrame)
            (JNIEnv *env, jobject result);

    jobject (JNICALL *NewGlobalRef)
            (JNIEnv *env, jobject lobj);
    void (JNICALL *DeleteGlobalRef)
            (JNIEnv *env, jobject gref);
    void (JNICALL *DeleteLocalRef)
            (JNIEnv *env, jobject obj);
    jboolean (JNICALL *IsSameObject)
            (JNIEnv *env, jobject obj1, jobject obj2);
    jobject (JNICALL *NewLocalRef)
            (JNIEnv *env, jobject ref);
    jint (JNICALL *EnsureLocalCapacity)
            (JNIEnv *env, jint capacity);

    jobject (JNICALL *AllocObject)
            (JNIEnv *env, jclass clazz);
    jobject (JNICALL *NewObject)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jobject (JNICALL *NewObjectV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jobject (JNICALL *NewObjectA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jclass (JNICALL *GetObjectClass)
            (JNIEnv *env, jobject obj);
    jboolean (JNICALL *IsInstanceOf)
            (JNIEnv *env, jobject obj, jclass clazz);

    jmethodID (JNICALL *GetMethodID)
            (JNIEnv *env, jclass clazz, const char *name, const char *sig);

    jobject (JNICALL *CallObjectMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jobject (JNICALL *CallObjectMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jobject (JNICALL *CallObjectMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue * args);

    jboolean (JNICALL *CallBooleanMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jboolean (JNICALL *CallBooleanMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jboolean (JNICALL *CallBooleanMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue * args);

    jbyte (JNICALL *CallByteMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jbyte (JNICALL *CallByteMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jbyte (JNICALL *CallByteMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    jchar (JNICALL *CallCharMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jchar (JNICALL *CallCharMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jchar (JNICALL *CallCharMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    jshort (JNICALL *CallShortMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jshort (JNICALL *CallShortMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jshort (JNICALL *CallShortMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    jint (JNICALL *CallIntMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jint (JNICALL *CallIntMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jint (JNICALL *CallIntMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    jlong (JNICALL *CallLongMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jlong (JNICALL *CallLongMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jlong (JNICALL *CallLongMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    jfloat (JNICALL *CallFloatMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jfloat (JNICALL *CallFloatMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jfloat (JNICALL *CallFloatMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    jdouble (JNICALL *CallDoubleMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    jdouble (JNICALL *CallDoubleMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    jdouble (JNICALL *CallDoubleMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args);

    void (JNICALL *CallVoidMethod)
            (JNIEnv *env, jobject obj, jmethodID methodID, ...);
    void (JNICALL *CallVoidMethodV)
            (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
    void (JNICALL *CallVoidMethodA)
            (JNIEnv *env, jobject obj, jmethodID methodID, const jvalue * args);

    jobject (JNICALL *CallNonvirtualObjectMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jobject (JNICALL *CallNonvirtualObjectMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jobject (JNICALL *CallNonvirtualObjectMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue * args);

    jboolean (JNICALL *CallNonvirtualBooleanMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jboolean (JNICALL *CallNonvirtualBooleanMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jboolean (JNICALL *CallNonvirtualBooleanMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue * args);

    jbyte (JNICALL *CallNonvirtualByteMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jbyte (JNICALL *CallNonvirtualByteMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jbyte (JNICALL *CallNonvirtualByteMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    jchar (JNICALL *CallNonvirtualCharMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jchar (JNICALL *CallNonvirtualCharMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jchar (JNICALL *CallNonvirtualCharMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    jshort (JNICALL *CallNonvirtualShortMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jshort (JNICALL *CallNonvirtualShortMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jshort (JNICALL *CallNonvirtualShortMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    jint (JNICALL *CallNonvirtualIntMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jint (JNICALL *CallNonvirtualIntMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jint (JNICALL *CallNonvirtualIntMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    jlong (JNICALL *CallNonvirtualLongMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jlong (JNICALL *CallNonvirtualLongMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jlong (JNICALL *CallNonvirtualLongMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    jfloat (JNICALL *CallNonvirtualFloatMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jfloat (JNICALL *CallNonvirtualFloatMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jfloat (JNICALL *CallNonvirtualFloatMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    jdouble (JNICALL *CallNonvirtualDoubleMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    jdouble (JNICALL *CallNonvirtualDoubleMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    jdouble (JNICALL *CallNonvirtualDoubleMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue *args);

    void (JNICALL *CallNonvirtualVoidMethod)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
    void (JNICALL *CallNonvirtualVoidMethodV)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             va_list args);
    void (JNICALL *CallNonvirtualVoidMethodA)
            (JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID,
             const jvalue * args);

    jfieldID (JNICALL *GetFieldID)
            (JNIEnv *env, jclass clazz, const char *name, const char *sig);

    jobject (JNICALL *GetObjectField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jboolean (JNICALL *GetBooleanField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jbyte (JNICALL *GetByteField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jchar (JNICALL *GetCharField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jshort (JNICALL *GetShortField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jint (JNICALL *GetIntField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jlong (JNICALL *GetLongField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jfloat (JNICALL *GetFloatField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);
    jdouble (JNICALL *GetDoubleField)
            (JNIEnv *env, jobject obj, jfieldID fieldID);

    void (JNICALL *SetObjectField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jobject val);
    void (JNICALL *SetBooleanField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jboolean val);
    void (JNICALL *SetByteField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jbyte val);
    void (JNICALL *SetCharField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jchar val);
    void (JNICALL *SetShortField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jshort val);
    void (JNICALL *SetIntField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jint val);
    void (JNICALL *SetLongField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jlong val);
    void (JNICALL *SetFloatField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jfloat val);
    void (JNICALL *SetDoubleField)
            (JNIEnv *env, jobject obj, jfieldID fieldID, jdouble val);

    jmethodID (JNICALL *GetStaticMethodID)
            (JNIEnv *env, jclass clazz, const char *name, const char *sig);

    jobject (JNICALL *CallStaticObjectMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jobject (JNICALL *CallStaticObjectMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jobject (JNICALL *CallStaticObjectMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jboolean (JNICALL *CallStaticBooleanMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jboolean (JNICALL *CallStaticBooleanMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jboolean (JNICALL *CallStaticBooleanMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jbyte (JNICALL *CallStaticByteMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jbyte (JNICALL *CallStaticByteMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jbyte (JNICALL *CallStaticByteMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jchar (JNICALL *CallStaticCharMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jchar (JNICALL *CallStaticCharMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jchar (JNICALL *CallStaticCharMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jshort (JNICALL *CallStaticShortMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jshort (JNICALL *CallStaticShortMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jshort (JNICALL *CallStaticShortMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jint (JNICALL *CallStaticIntMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jint (JNICALL *CallStaticIntMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jint (JNICALL *CallStaticIntMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jlong (JNICALL *CallStaticLongMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jlong (JNICALL *CallStaticLongMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jlong (JNICALL *CallStaticLongMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jfloat (JNICALL *CallStaticFloatMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jfloat (JNICALL *CallStaticFloatMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jfloat (JNICALL *CallStaticFloatMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    jdouble (JNICALL *CallStaticDoubleMethod)
            (JNIEnv *env, jclass clazz, jmethodID methodID, ...);
    jdouble (JNICALL *CallStaticDoubleMethodV)
            (JNIEnv *env, jclass clazz, jmethodID methodID, va_list args);
    jdouble (JNICALL *CallStaticDoubleMethodA)
            (JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args);

    void (JNICALL *CallStaticVoidMethod)
            (JNIEnv *env, jclass cls, jmethodID methodID, ...);
    void (JNICALL *CallStaticVoidMethodV)
            (JNIEnv *env, jclass cls, jmethodID methodID, va_list args);
    void (JNICALL *CallStaticVoidMethodA)
            (JNIEnv *env, jclass cls, jmethodID methodID, const jvalue * args);

    jfieldID (JNICALL *GetStaticFieldID)
            (JNIEnv *env, jclass clazz, const char *name, const char *sig);
    jobject (JNICALL *GetStaticObjectField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jboolean (JNICALL *GetStaticBooleanField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jbyte (JNICALL *GetStaticByteField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jchar (JNICALL *GetStaticCharField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jshort (JNICALL *GetStaticShortField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jint (JNICALL *GetStaticIntField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jlong (JNICALL *GetStaticLongField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jfloat (JNICALL *GetStaticFloatField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);
    jdouble (JNICALL *GetStaticDoubleField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID);

    void (JNICALL *SetStaticObjectField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jobject value);
    void (JNICALL *SetStaticBooleanField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jboolean value);
    void (JNICALL *SetStaticByteField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jbyte value);
    void (JNICALL *SetStaticCharField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jchar value);
    void (JNICALL *SetStaticShortField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jshort value);
    void (JNICALL *SetStaticIntField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jint value);
    void (JNICALL *SetStaticLongField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jlong value);
    void (JNICALL *SetStaticFloatField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jfloat value);
    void (JNICALL *SetStaticDoubleField)
            (JNIEnv *env, jclass clazz, jfieldID fieldID, jdouble value);

    jstring (JNICALL *NewString)
            (JNIEnv *env, const jchar *unicode, jsize len);
    jsize (JNICALL *GetStringLength)
            (JNIEnv *env, jstring str);
    const jchar *(JNICALL *GetStringChars)
            (JNIEnv *env, jstring str, jboolean *isCopy);
    void (JNICALL *ReleaseStringChars)
            (JNIEnv *env, jstring str, const jchar *chars);

    jstring (JNICALL *NewStringUTF)
            (JNIEnv *env, const char *utf);
    jsize (JNICALL *GetStringUTFLength)
            (JNIEnv *env, jstring str);
    const char* (JNICALL *GetStringUTFChars)
            (JNIEnv *env, jstring str, jboolean *isCopy);
    void (JNICALL *ReleaseStringUTFChars)
            (JNIEnv *env, jstring str, const char* chars);


    jsize (JNICALL *GetArrayLength)
            (JNIEnv *env, jarray array);

    jobjectArray (JNICALL *NewObjectArray)
            (JNIEnv *env, jsize len, jclass clazz, jobject init);
    jobject (JNICALL *GetObjectArrayElement)
            (JNIEnv *env, jobjectArray array, jsize index);
    void (JNICALL *SetObjectArrayElement)
            (JNIEnv *env, jobjectArray array, jsize index, jobject val);

    jbooleanArray (JNICALL *NewBooleanArray)
            (JNIEnv *env, jsize len);
    jbyteArray (JNICALL *NewByteArray)
            (JNIEnv *env, jsize len);
    jcharArray (JNICALL *NewCharArray)
            (JNIEnv *env, jsize len);
    jshortArray (JNICALL *NewShortArray)
            (JNIEnv *env, jsize len);
    jintArray (JNICALL *NewIntArray)
            (JNIEnv *env, jsize len);
    jlongArray (JNICALL *NewLongArray)
            (JNIEnv *env, jsize len);
    jfloatArray (JNICALL *NewFloatArray)
            (JNIEnv *env, jsize len);
    jdoubleArray (JNICALL *NewDoubleArray)
            (JNIEnv *env, jsize len);

    jboolean * (JNICALL *GetBooleanArrayElements)
            (JNIEnv *env, jbooleanArray array, jboolean *isCopy);
    jbyte * (JNICALL *GetByteArrayElements)
            (JNIEnv *env, jbyteArray array, jboolean *isCopy);
    jchar * (JNICALL *GetCharArrayElements)
            (JNIEnv *env, jcharArray array, jboolean *isCopy);
    jshort * (JNICALL *GetShortArrayElements)
            (JNIEnv *env, jshortArray array, jboolean *isCopy);
    jint * (JNICALL *GetIntArrayElements)
            (JNIEnv *env, jintArray array, jboolean *isCopy);
    jlong * (JNICALL *GetLongArrayElements)
            (JNIEnv *env, jlongArray array, jboolean *isCopy);
    jfloat * (JNICALL *GetFloatArrayElements)
            (JNIEnv *env, jfloatArray array, jboolean *isCopy);
    jdouble * (JNICALL *GetDoubleArrayElements)
            (JNIEnv *env, jdoubleArray array, jboolean *isCopy);

    void (JNICALL *ReleaseBooleanArrayElements)
            (JNIEnv *env, jbooleanArray array, jboolean *elems, jint mode);
    void (JNICALL *ReleaseByteArrayElements)
            (JNIEnv *env, jbyteArray array, jbyte *elems, jint mode);
    void (JNICALL *ReleaseCharArrayElements)
            (JNIEnv *env, jcharArray array, jchar *elems, jint mode);
    void (JNICALL *ReleaseShortArrayElements)
            (JNIEnv *env, jshortArray array, jshort *elems, jint mode);
    void (JNICALL *ReleaseIntArrayElements)
            (JNIEnv *env, jintArray array, jint *elems, jint mode);
    void (JNICALL *ReleaseLongArrayElements)
            (JNIEnv *env, jlongArray array, jlong *elems, jint mode);
    void (JNICALL *ReleaseFloatArrayElements)
            (JNIEnv *env, jfloatArray array, jfloat *elems, jint mode);
    void (JNICALL *ReleaseDoubleArrayElements)
            (JNIEnv *env, jdoubleArray array, jdouble *elems, jint mode);

    void (JNICALL *GetBooleanArrayRegion)
            (JNIEnv *env, jbooleanArray array, jsize start, jsize l, jboolean *buf);
    void (JNICALL *GetByteArrayRegion)
            (JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf);
    void (JNICALL *GetCharArrayRegion)
            (JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf);
    void (JNICALL *GetShortArrayRegion)
            (JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf);
    void (JNICALL *GetIntArrayRegion)
            (JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf);
    void (JNICALL *GetLongArrayRegion)
            (JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf);
    void (JNICALL *GetFloatArrayRegion)
            (JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf);
    void (JNICALL *GetDoubleArrayRegion)
            (JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf);

    void (JNICALL *SetBooleanArrayRegion)
            (JNIEnv *env, jbooleanArray array, jsize start, jsize l, const jboolean *buf);
    void (JNICALL *SetByteArrayRegion)
            (JNIEnv *env, jbyteArray array, jsize start, jsize len, const jbyte *buf);
    void (JNICALL *SetCharArrayRegion)
            (JNIEnv *env, jcharArray array, jsize start, jsize len, const jchar *buf);
    void (JNICALL *SetShortArrayRegion)
            (JNIEnv *env, jshortArray array, jsize start, jsize len, const jshort *buf);
    void (JNICALL *SetIntArrayRegion)
            (JNIEnv *env, jintArray array, jsize start, jsize len, const jint *buf);
    void (JNICALL *SetLongArrayRegion)
            (JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf);
    void (JNICALL *SetFloatArrayRegion)
            (JNIEnv *env, jfloatArray array, jsize start, jsize len, const jfloat *buf);
    void (JNICALL *SetDoubleArrayRegion)
            (JNIEnv *env, jdoubleArray array, jsize start, jsize len, const jdouble *buf);

    jint (JNICALL *RegisterNatives)
            (JNIEnv *env, jclass clazz, const JNINativeMethod *methods,
             jint nMethods);
    jint (JNICALL *UnregisterNatives)
            (JNIEnv *env, jclass clazz);

    jint (JNICALL *MonitorEnter)
            (JNIEnv *env, jobject obj);
    jint (JNICALL *MonitorExit)
            (JNIEnv *env, jobject obj);

    jint (JNICALL *GetJavaVM)
            (JNIEnv *env, JavaVM **vm);

    void (JNICALL *GetStringRegion)
            (JNIEnv *env, jstring str, jsize start, jsize len, jchar *buf);
    void (JNICALL *GetStringUTFRegion)
            (JNIEnv *env, jstring str, jsize start, jsize len, char *buf);

    void * (JNICALL *GetPrimitiveArrayCritical)
            (JNIEnv *env, jarray array, jboolean *isCopy);
    void (JNICALL *ReleasePrimitiveArrayCritical)
            (JNIEnv *env, jarray array, void *carray, jint mode);

    const jchar * (JNICALL *GetStringCritical)
            (JNIEnv *env, jstring string, jboolean *isCopy);
    void (JNICALL *ReleaseStringCritical)
            (JNIEnv *env, jstring string, const jchar *cstring);

    jweak (JNICALL *NewWeakGlobalRef)
            (JNIEnv *env, jobject obj);
    void (JNICALL *DeleteWeakGlobalRef)
            (JNIEnv *env, jweak ref);

    jboolean (JNICALL *ExceptionCheck)
            (JNIEnv *env);

    jobject (JNICALL *NewDirectByteBuffer)
            (JNIEnv* env, void* address, jlong capacity);
    void* (JNICALL *GetDirectBufferAddress)
            (JNIEnv* env, jobject buf);
    jlong (JNICALL *GetDirectBufferCapacity)
            (JNIEnv* env, jobject buf);

    /* New JNI 1.6 Features */

    jobjectRefType (JNICALL *GetObjectRefType)
            (JNIEnv* env, jobject obj);

    /* Module Features */

    jobject (JNICALL *GetModule)
            (JNIEnv* env, jclass clazz);
};

/*
 * We use inlined functions for C++ so that programmers can write:
 *
 *    env->FindClass("java/lang/String")
 *
 * in C++ rather than:
 *
 *    (*env)->FindClass(env, "java/lang/String")
 *
 * in C.
 */

struct JNIEnv_ {
    const struct JNINativeInterface_ *functions;
#ifdef __cplusplus

    JNI_FORCEINLINE jint GetVersion() {
        return functions->GetVersion(this);
    }
    JNI_FORCEINLINE jclass DefineClass(const char *name, jobject loader, const jbyte *buf,
                       jsize len) {
        return functions->DefineClass(this, name, loader, buf, len);
    }
    JNI_FORCEINLINE jclass FindClass(const char *name) {
        return functions->FindClass(this, name);
    }
    JNI_FORCEINLINE jmethodID FromReflectedMethod(jobject method) {
        return functions->FromReflectedMethod(this,method);
    }
    JNI_FORCEINLINE jfieldID FromReflectedField(jobject field) {
        return functions->FromReflectedField(this,field);
    }

    JNI_FORCEINLINE jobject ToReflectedMethod(jclass cls, jmethodID methodID, jboolean isStatic) {
        return functions->ToReflectedMethod(this, cls, methodID, isStatic);
    }

    JNI_FORCEINLINE jclass GetSuperclass(jclass sub) {
        return functions->GetSuperclass(this, sub);
    }
    JNI_FORCEINLINE jboolean IsAssignableFrom(jclass sub, jclass sup) {
        return functions->IsAssignableFrom(this, sub, sup);
    }

    JNI_FORCEINLINE jobject ToReflectedField(jclass cls, jfieldID fieldID, jboolean isStatic) {
        return functions->ToReflectedField(this,cls,fieldID,isStatic);
    }

    JNI_FORCEINLINE jint Throw(jthrowable obj) {
        return functions->Throw(this, obj);
    }
    JNI_FORCEINLINE jint ThrowNew(jclass clazz, const char *msg) {
        return functions->ThrowNew(this, clazz, msg);
    }
    JNI_FORCEINLINE jthrowable ExceptionOccurred() {
        return functions->ExceptionOccurred(this);
    }
    JNI_FORCEINLINE void ExceptionDescribe() {
        functions->ExceptionDescribe(this);
    }
    JNI_FORCEINLINE void ExceptionClear() {
        functions->ExceptionClear(this);
    }
    JNI_FORCEINLINE void FatalError(const char *msg) {
        functions->FatalError(this, msg);
    }

    JNI_FORCEINLINE jint PushLocalFrame(jint capacity) {
        return functions->PushLocalFrame(this,capacity);
    }
    JNI_FORCEINLINE jobject PopLocalFrame(jobject result) {
        return functions->PopLocalFrame(this,result);
    }

    JNI_FORCEINLINE jobject NewGlobalRef(jobject lobj) {
        return functions->NewGlobalRef(this,lobj);
    }
    JNI_FORCEINLINE void DeleteGlobalRef(jobject gref) {
        functions->DeleteGlobalRef(this,gref);
    }
    JNI_FORCEINLINE void DeleteLocalRef(jobject obj) {
        functions->DeleteLocalRef(this, obj);
    }

    JNI_FORCEINLINE jboolean IsSameObject(jobject obj1, jobject obj2) {
        return functions->IsSameObject(this,obj1,obj2);
    }

    JNI_FORCEINLINE jobject NewLocalRef(jobject ref) {
        return functions->NewLocalRef(this,ref);
    }
    JNI_FORCEINLINE jint EnsureLocalCapacity(jint capacity) {
        return functions->EnsureLocalCapacity(this,capacity);
    }

    JNI_FORCEINLINE jobject AllocObject(jclass clazz) {
        return functions->AllocObject(this,clazz);
    }
    JNI_FORCEINLINE jobject NewObject(jclass clazz, jmethodID methodID, ...) {
        va_list args;
        jobject result;
        va_start(args, methodID);
        result = functions->NewObjectV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jobject NewObjectV(jclass clazz, jmethodID methodID,
                       va_list args) {
        return functions->NewObjectV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jobject NewObjectA(jclass clazz, jmethodID methodID,
                       const jvalue *args) {
        return functions->NewObjectA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jclass GetObjectClass(jobject obj) {
        return functions->GetObjectClass(this,obj);
    }
    JNI_FORCEINLINE jboolean IsInstanceOf(jobject obj, jclass clazz) {
        return functions->IsInstanceOf(this,obj,clazz);
    }

    JNI_FORCEINLINE jmethodID GetMethodID(jclass clazz, const char *name,
                          const char *sig) {
        return functions->GetMethodID(this,clazz,name,sig);
    }

    JNI_FORCEINLINE jobject CallObjectMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jobject result;
        va_start(args,methodID);
        result = functions->CallObjectMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jobject CallObjectMethodV(jobject obj, jmethodID methodID,
                        va_list args) {
        return functions->CallObjectMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jobject CallObjectMethodA(jobject obj, jmethodID methodID,
                        const jvalue * args) {
        return functions->CallObjectMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jboolean CallBooleanMethod(jobject obj,
                               jmethodID methodID, ...) {
        va_list args;
        jboolean result;
        va_start(args,methodID);
        result = functions->CallBooleanMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jboolean CallBooleanMethodV(jobject obj, jmethodID methodID,
                                va_list args) {
        return functions->CallBooleanMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jboolean CallBooleanMethodA(jobject obj, jmethodID methodID,
                                const jvalue * args) {
        return functions->CallBooleanMethodA(this,obj,methodID, args);
    }

    JNI_FORCEINLINE jbyte CallByteMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jbyte result;
        va_start(args,methodID);
        result = functions->CallByteMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jbyte CallByteMethodV(jobject obj, jmethodID methodID,
                          va_list args) {
        return functions->CallByteMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jbyte CallByteMethodA(jobject obj, jmethodID methodID,
                          const jvalue * args) {
        return functions->CallByteMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jchar CallCharMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jchar result;
        va_start(args,methodID);
        result = functions->CallCharMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jchar CallCharMethodV(jobject obj, jmethodID methodID,
                          va_list args) {
        return functions->CallCharMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jchar CallCharMethodA(jobject obj, jmethodID methodID,
                          const jvalue * args) {
        return functions->CallCharMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jshort CallShortMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jshort result;
        va_start(args,methodID);
        result = functions->CallShortMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jshort CallShortMethodV(jobject obj, jmethodID methodID,
                            va_list args) {
        return functions->CallShortMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jshort CallShortMethodA(jobject obj, jmethodID methodID,
                            const jvalue * args) {
        return functions->CallShortMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jint CallIntMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jint result;
        va_start(args,methodID);
        result = functions->CallIntMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jint CallIntMethodV(jobject obj, jmethodID methodID,
                        va_list args) {
        return functions->CallIntMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jint CallIntMethodA(jobject obj, jmethodID methodID,
                        const jvalue * args) {
        return functions->CallIntMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jlong CallLongMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jlong result;
        va_start(args,methodID);
        result = functions->CallLongMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jlong CallLongMethodV(jobject obj, jmethodID methodID,
                          va_list args) {
        return functions->CallLongMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jlong CallLongMethodA(jobject obj, jmethodID methodID,
                          const jvalue * args) {
        return functions->CallLongMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jfloat CallFloatMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jfloat result;
        va_start(args,methodID);
        result = functions->CallFloatMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jfloat CallFloatMethodV(jobject obj, jmethodID methodID,
                            va_list args) {
        return functions->CallFloatMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jfloat CallFloatMethodA(jobject obj, jmethodID methodID,
                            const jvalue * args) {
        return functions->CallFloatMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jdouble CallDoubleMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        jdouble result;
        va_start(args,methodID);
        result = functions->CallDoubleMethodV(this,obj,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jdouble CallDoubleMethodV(jobject obj, jmethodID methodID,
                        va_list args) {
        return functions->CallDoubleMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE jdouble CallDoubleMethodA(jobject obj, jmethodID methodID,
                        const jvalue * args) {
        return functions->CallDoubleMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE void CallVoidMethod(jobject obj, jmethodID methodID, ...) {
        va_list args;
        va_start(args,methodID);
        functions->CallVoidMethodV(this,obj,methodID,args);
        va_end(args);
    }
    JNI_FORCEINLINE void CallVoidMethodV(jobject obj, jmethodID methodID,
                         va_list args) {
        functions->CallVoidMethodV(this,obj,methodID,args);
    }
    JNI_FORCEINLINE void CallVoidMethodA(jobject obj, jmethodID methodID,
                         const jvalue * args) {
        functions->CallVoidMethodA(this,obj,methodID,args);
    }

    JNI_FORCEINLINE jobject CallNonvirtualObjectMethod(jobject obj, jclass clazz,
                                       jmethodID methodID, ...) {
        va_list args;
        jobject result;
        va_start(args,methodID);
        result = functions->CallNonvirtualObjectMethodV(this,obj,clazz,
                                                        methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jobject CallNonvirtualObjectMethodV(jobject obj, jclass clazz,
                                        jmethodID methodID, va_list args) {
        return functions->CallNonvirtualObjectMethodV(this,obj,clazz,
                                                      methodID,args);
    }
    JNI_FORCEINLINE jobject CallNonvirtualObjectMethodA(jobject obj, jclass clazz,
                                        jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualObjectMethodA(this,obj,clazz,
                                                      methodID,args);
    }

    JNI_FORCEINLINE jboolean CallNonvirtualBooleanMethod(jobject obj, jclass clazz,
                                         jmethodID methodID, ...) {
        va_list args;
        jboolean result;
        va_start(args,methodID);
        result = functions->CallNonvirtualBooleanMethodV(this,obj,clazz,
                                                         methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jboolean CallNonvirtualBooleanMethodV(jobject obj, jclass clazz,
                                          jmethodID methodID, va_list args) {
        return functions->CallNonvirtualBooleanMethodV(this,obj,clazz,
                                                       methodID,args);
    }
    JNI_FORCEINLINE jboolean CallNonvirtualBooleanMethodA(jobject obj, jclass clazz,
                                          jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualBooleanMethodA(this,obj,clazz,
                                                       methodID, args);
    }

    JNI_FORCEINLINE jbyte CallNonvirtualByteMethod(jobject obj, jclass clazz,
                                   jmethodID methodID, ...) {
        va_list args;
        jbyte result;
        va_start(args,methodID);
        result = functions->CallNonvirtualByteMethodV(this,obj,clazz,
                                                      methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jbyte CallNonvirtualByteMethodV(jobject obj, jclass clazz,
                                    jmethodID methodID, va_list args) {
        return functions->CallNonvirtualByteMethodV(this,obj,clazz,
                                                    methodID,args);
    }
    JNI_FORCEINLINE jbyte CallNonvirtualByteMethodA(jobject obj, jclass clazz,
                                    jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualByteMethodA(this,obj,clazz,
                                                    methodID,args);
    }

    JNI_FORCEINLINE jchar CallNonvirtualCharMethod(jobject obj, jclass clazz,
                                   jmethodID methodID, ...) {
        va_list args;
        jchar result;
        va_start(args,methodID);
        result = functions->CallNonvirtualCharMethodV(this,obj,clazz,
                                                      methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jchar CallNonvirtualCharMethodV(jobject obj, jclass clazz,
                                    jmethodID methodID, va_list args) {
        return functions->CallNonvirtualCharMethodV(this,obj,clazz,
                                                    methodID,args);
    }
    JNI_FORCEINLINE jchar CallNonvirtualCharMethodA(jobject obj, jclass clazz,
                                    jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualCharMethodA(this,obj,clazz,
                                                    methodID,args);
    }

    JNI_FORCEINLINE jshort CallNonvirtualShortMethod(jobject obj, jclass clazz,
                                     jmethodID methodID, ...) {
        va_list args;
        jshort result;
        va_start(args,methodID);
        result = functions->CallNonvirtualShortMethodV(this,obj,clazz,
                                                       methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jshort CallNonvirtualShortMethodV(jobject obj, jclass clazz,
                                      jmethodID methodID, va_list args) {
        return functions->CallNonvirtualShortMethodV(this,obj,clazz,
                                                     methodID,args);
    }
    JNI_FORCEINLINE jshort CallNonvirtualShortMethodA(jobject obj, jclass clazz,
                                      jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualShortMethodA(this,obj,clazz,
                                                     methodID,args);
    }

    JNI_FORCEINLINE jint CallNonvirtualIntMethod(jobject obj, jclass clazz,
                                 jmethodID methodID, ...) {
        va_list args;
        jint result;
        va_start(args,methodID);
        result = functions->CallNonvirtualIntMethodV(this,obj,clazz,
                                                     methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jint CallNonvirtualIntMethodV(jobject obj, jclass clazz,
                                  jmethodID methodID, va_list args) {
        return functions->CallNonvirtualIntMethodV(this,obj,clazz,
                                                   methodID,args);
    }
    JNI_FORCEINLINE jint CallNonvirtualIntMethodA(jobject obj, jclass clazz,
                                  jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualIntMethodA(this,obj,clazz,
                                                   methodID,args);
    }

    JNI_FORCEINLINE jlong CallNonvirtualLongMethod(jobject obj, jclass clazz,
                                   jmethodID methodID, ...) {
        va_list args;
        jlong result;
        va_start(args,methodID);
        result = functions->CallNonvirtualLongMethodV(this,obj,clazz,
                                                      methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jlong CallNonvirtualLongMethodV(jobject obj, jclass clazz,
                                    jmethodID methodID, va_list args) {
        return functions->CallNonvirtualLongMethodV(this,obj,clazz,
                                                    methodID,args);
    }
    JNI_FORCEINLINE jlong CallNonvirtualLongMethodA(jobject obj, jclass clazz,
                                    jmethodID methodID, const jvalue * args) {
        return functions->CallNonvirtualLongMethodA(this,obj,clazz,
                                                    methodID,args);
    }

    JNI_FORCEINLINE jfloat CallNonvirtualFloatMethod(jobject obj, jclass clazz,
                                     jmethodID methodID, ...) {
        va_list args;
        jfloat result;
        va_start(args,methodID);
        result = functions->CallNonvirtualFloatMethodV(this,obj,clazz,
                                                       methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jfloat CallNonvirtualFloatMethodV(jobject obj, jclass clazz,
                                      jmethodID methodID,
                                      va_list args) {
        return functions->CallNonvirtualFloatMethodV(this,obj,clazz,
                                                     methodID,args);
    }
    JNI_FORCEINLINE jfloat CallNonvirtualFloatMethodA(jobject obj, jclass clazz,
                                      jmethodID methodID,
                                      const jvalue * args) {
        return functions->CallNonvirtualFloatMethodA(this,obj,clazz,
                                                     methodID,args);
    }

    JNI_FORCEINLINE jdouble CallNonvirtualDoubleMethod(jobject obj, jclass clazz,
                                       jmethodID methodID, ...) {
        va_list args;
        jdouble result;
        va_start(args,methodID);
        result = functions->CallNonvirtualDoubleMethodV(this,obj,clazz,
                                                        methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jdouble CallNonvirtualDoubleMethodV(jobject obj, jclass clazz,
                                        jmethodID methodID,
                                        va_list args) {
        return functions->CallNonvirtualDoubleMethodV(this,obj,clazz,
                                                      methodID,args);
    }
    JNI_FORCEINLINE jdouble CallNonvirtualDoubleMethodA(jobject obj, jclass clazz,
                                        jmethodID methodID,
                                        const jvalue * args) {
        return functions->CallNonvirtualDoubleMethodA(this,obj,clazz,
                                                      methodID,args);
    }

    JNI_FORCEINLINE void CallNonvirtualVoidMethod(jobject obj, jclass clazz,
                                  jmethodID methodID, ...) {
        va_list args;
        va_start(args,methodID);
        functions->CallNonvirtualVoidMethodV(this,obj,clazz,methodID,args);
        va_end(args);
    }
    JNI_FORCEINLINE void CallNonvirtualVoidMethodV(jobject obj, jclass clazz,
                                   jmethodID methodID,
                                   va_list args) {
        functions->CallNonvirtualVoidMethodV(this,obj,clazz,methodID,args);
    }
    JNI_FORCEINLINE void CallNonvirtualVoidMethodA(jobject obj, jclass clazz,
                                   jmethodID methodID,
                                   const jvalue * args) {
        functions->CallNonvirtualVoidMethodA(this,obj,clazz,methodID,args);
    }

    JNI_FORCEINLINE jfieldID GetFieldID(jclass clazz, const char *name,
                        const char *sig) {
        return functions->GetFieldID(this,clazz,name,sig);
    }

    JNI_FORCEINLINE jobject GetObjectField(jobject obj, jfieldID fieldID) {
        return functions->GetObjectField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jboolean GetBooleanField(jobject obj, jfieldID fieldID) {
        return functions->GetBooleanField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jbyte GetByteField(jobject obj, jfieldID fieldID) {
        return functions->GetByteField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jchar GetCharField(jobject obj, jfieldID fieldID) {
        return functions->GetCharField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jshort GetShortField(jobject obj, jfieldID fieldID) {
        return functions->GetShortField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jint GetIntField(jobject obj, jfieldID fieldID) {
        return functions->GetIntField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jlong GetLongField(jobject obj, jfieldID fieldID) {
        return functions->GetLongField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jfloat GetFloatField(jobject obj, jfieldID fieldID) {
        return functions->GetFloatField(this,obj,fieldID);
    }
    JNI_FORCEINLINE jdouble GetDoubleField(jobject obj, jfieldID fieldID) {
        return functions->GetDoubleField(this,obj,fieldID);
    }

    JNI_FORCEINLINE void SetObjectField(jobject obj, jfieldID fieldID, jobject val) {
        functions->SetObjectField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetBooleanField(jobject obj, jfieldID fieldID,
                         jboolean val) {
        functions->SetBooleanField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetByteField(jobject obj, jfieldID fieldID,
                      jbyte val) {
        functions->SetByteField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetCharField(jobject obj, jfieldID fieldID,
                      jchar val) {
        functions->SetCharField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetShortField(jobject obj, jfieldID fieldID,
                       jshort val) {
        functions->SetShortField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetIntField(jobject obj, jfieldID fieldID,
                     jint val) {
        functions->SetIntField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetLongField(jobject obj, jfieldID fieldID,
                      jlong val) {
        functions->SetLongField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetFloatField(jobject obj, jfieldID fieldID,
                       jfloat val) {
        functions->SetFloatField(this,obj,fieldID,val);
    }
    JNI_FORCEINLINE void SetDoubleField(jobject obj, jfieldID fieldID,
                        jdouble val) {
        functions->SetDoubleField(this,obj,fieldID,val);
    }

    JNI_FORCEINLINE jmethodID GetStaticMethodID(jclass clazz, const char *name,
                                const char *sig) {
        return functions->GetStaticMethodID(this,clazz,name,sig);
    }

    JNI_FORCEINLINE jobject CallStaticObjectMethod(jclass clazz, jmethodID methodID,
                             ...) {
        va_list args;
        jobject result;
        va_start(args,methodID);
        result = functions->CallStaticObjectMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jobject CallStaticObjectMethodV(jclass clazz, jmethodID methodID,
                              va_list args) {
        return functions->CallStaticObjectMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jobject CallStaticObjectMethodA(jclass clazz, jmethodID methodID,
                              const jvalue *args) {
        return functions->CallStaticObjectMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jboolean CallStaticBooleanMethod(jclass clazz,
                                     jmethodID methodID, ...) {
        va_list args;
        jboolean result;
        va_start(args,methodID);
        result = functions->CallStaticBooleanMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jboolean CallStaticBooleanMethodV(jclass clazz,
                                      jmethodID methodID, va_list args) {
        return functions->CallStaticBooleanMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jboolean CallStaticBooleanMethodA(jclass clazz,
                                      jmethodID methodID, const jvalue *args) {
        return functions->CallStaticBooleanMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jbyte CallStaticByteMethod(jclass clazz,
                               jmethodID methodID, ...) {
        va_list args;
        jbyte result;
        va_start(args,methodID);
        result = functions->CallStaticByteMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jbyte CallStaticByteMethodV(jclass clazz,
                                jmethodID methodID, va_list args) {
        return functions->CallStaticByteMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jbyte CallStaticByteMethodA(jclass clazz,
                                jmethodID methodID, const jvalue *args) {
        return functions->CallStaticByteMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jchar CallStaticCharMethod(jclass clazz,
                               jmethodID methodID, ...) {
        va_list args;
        jchar result;
        va_start(args,methodID);
        result = functions->CallStaticCharMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jchar CallStaticCharMethodV(jclass clazz,
                                jmethodID methodID, va_list args) {
        return functions->CallStaticCharMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jchar CallStaticCharMethodA(jclass clazz,
                                jmethodID methodID, const jvalue *args) {
        return functions->CallStaticCharMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jshort CallStaticShortMethod(jclass clazz,
                                 jmethodID methodID, ...) {
        va_list args;
        jshort result;
        va_start(args,methodID);
        result = functions->CallStaticShortMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jshort CallStaticShortMethodV(jclass clazz,
                                  jmethodID methodID, va_list args) {
        return functions->CallStaticShortMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jshort CallStaticShortMethodA(jclass clazz,
                                  jmethodID methodID, const jvalue *args) {
        return functions->CallStaticShortMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jint CallStaticIntMethod(jclass clazz,
                             jmethodID methodID, ...) {
        va_list args;
        jint result;
        va_start(args,methodID);
        result = functions->CallStaticIntMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jint CallStaticIntMethodV(jclass clazz,
                              jmethodID methodID, va_list args) {
        return functions->CallStaticIntMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jint CallStaticIntMethodA(jclass clazz,
                              jmethodID methodID, const jvalue *args) {
        return functions->CallStaticIntMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jlong CallStaticLongMethod(jclass clazz,
                               jmethodID methodID, ...) {
        va_list args;
        jlong result;
        va_start(args,methodID);
        result = functions->CallStaticLongMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jlong CallStaticLongMethodV(jclass clazz,
                                jmethodID methodID, va_list args) {
        return functions->CallStaticLongMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jlong CallStaticLongMethodA(jclass clazz,
                                jmethodID methodID, const jvalue *args) {
        return functions->CallStaticLongMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jfloat CallStaticFloatMethod(jclass clazz,
                                 jmethodID methodID, ...) {
        va_list args;
        jfloat result;
        va_start(args,methodID);
        result = functions->CallStaticFloatMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jfloat CallStaticFloatMethodV(jclass clazz,
                                  jmethodID methodID, va_list args) {
        return functions->CallStaticFloatMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jfloat CallStaticFloatMethodA(jclass clazz,
                                  jmethodID methodID, const jvalue *args) {
        return functions->CallStaticFloatMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE jdouble CallStaticDoubleMethod(jclass clazz,
                                   jmethodID methodID, ...) {
        va_list args;
        jdouble result;
        va_start(args,methodID);
        result = functions->CallStaticDoubleMethodV(this,clazz,methodID,args);
        va_end(args);
        return result;
    }
    JNI_FORCEINLINE jdouble CallStaticDoubleMethodV(jclass clazz,
                                    jmethodID methodID, va_list args) {
        return functions->CallStaticDoubleMethodV(this,clazz,methodID,args);
    }
    JNI_FORCEINLINE jdouble CallStaticDoubleMethodA(jclass clazz,
                                    jmethodID methodID, const jvalue *args) {
        return functions->CallStaticDoubleMethodA(this,clazz,methodID,args);
    }

    JNI_FORCEINLINE void CallStaticVoidMethod(jclass cls, jmethodID methodID, ...) {
        va_list args;
        va_start(args,methodID);
        functions->CallStaticVoidMethodV(this,cls,methodID,args);
        va_end(args);
    }
    JNI_FORCEINLINE void CallStaticVoidMethodV(jclass cls, jmethodID methodID,
                               va_list args) {
        functions->CallStaticVoidMethodV(this,cls,methodID,args);
    }
    JNI_FORCEINLINE void CallStaticVoidMethodA(jclass cls, jmethodID methodID,
                               const jvalue * args) {
        functions->CallStaticVoidMethodA(this,cls,methodID,args);
    }

    JNI_FORCEINLINE jfieldID GetStaticFieldID(jclass clazz, const char *name,
                              const char *sig) {
        return functions->GetStaticFieldID(this,clazz,name,sig);
    }
    JNI_FORCEINLINE jobject GetStaticObjectField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticObjectField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jboolean GetStaticBooleanField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticBooleanField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jbyte GetStaticByteField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticByteField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jchar GetStaticCharField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticCharField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jshort GetStaticShortField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticShortField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jint GetStaticIntField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticIntField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jlong GetStaticLongField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticLongField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jfloat GetStaticFloatField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticFloatField(this,clazz,fieldID);
    }
    JNI_FORCEINLINE jdouble GetStaticDoubleField(jclass clazz, jfieldID fieldID) {
        return functions->GetStaticDoubleField(this,clazz,fieldID);
    }

    JNI_FORCEINLINE void SetStaticObjectField(jclass clazz, jfieldID fieldID,
                        jobject value) {
      functions->SetStaticObjectField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticBooleanField(jclass clazz, jfieldID fieldID,
                        jboolean value) {
      functions->SetStaticBooleanField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticByteField(jclass clazz, jfieldID fieldID,
                        jbyte value) {
      functions->SetStaticByteField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticCharField(jclass clazz, jfieldID fieldID,
                        jchar value) {
      functions->SetStaticCharField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticShortField(jclass clazz, jfieldID fieldID,
                        jshort value) {
      functions->SetStaticShortField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticIntField(jclass clazz, jfieldID fieldID,
                        jint value) {
      functions->SetStaticIntField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticLongField(jclass clazz, jfieldID fieldID,
                        jlong value) {
      functions->SetStaticLongField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticFloatField(jclass clazz, jfieldID fieldID,
                        jfloat value) {
      functions->SetStaticFloatField(this,clazz,fieldID,value);
    }
    JNI_FORCEINLINE void SetStaticDoubleField(jclass clazz, jfieldID fieldID,
                        jdouble value) {
      functions->SetStaticDoubleField(this,clazz,fieldID,value);
    }

    JNI_FORCEINLINE jstring NewString(const jchar *unicode, jsize len) {
        return functions->NewString(this,unicode,len);
    }
    JNI_FORCEINLINE jsize GetStringLength(jstring str) {
        return functions->GetStringLength(this,str);
    }
    JNI_FORCEINLINE const jchar *GetStringChars(jstring str, jboolean *isCopy) {
        return functions->GetStringChars(this,str,isCopy);
    }
    JNI_FORCEINLINE void ReleaseStringChars(jstring str, const jchar *chars) {
        functions->ReleaseStringChars(this,str,chars);
    }

    JNI_FORCEINLINE jstring NewStringUTF(const char *utf) {
        return functions->NewStringUTF(this,utf);
    }
    JNI_FORCEINLINE jsize GetStringUTFLength(jstring str) {
        return functions->GetStringUTFLength(this,str);
    }
    JNI_FORCEINLINE const char* GetStringUTFChars(jstring str, jboolean *isCopy) {
        return functions->GetStringUTFChars(this,str,isCopy);
    }
    JNI_FORCEINLINE void ReleaseStringUTFChars(jstring str, const char* chars) {
        functions->ReleaseStringUTFChars(this,str,chars);
    }

    JNI_FORCEINLINE jsize GetArrayLength(jarray array) {
        return functions->GetArrayLength(this,array);
    }

    JNI_FORCEINLINE jobjectArray NewObjectArray(jsize len, jclass clazz,
                                jobject init) {
        return functions->NewObjectArray(this,len,clazz,init);
    }
    JNI_FORCEINLINE jobject GetObjectArrayElement(jobjectArray array, jsize index) {
        return functions->GetObjectArrayElement(this,array,index);
    }
    JNI_FORCEINLINE void SetObjectArrayElement(jobjectArray array, jsize index,
                               jobject val) {
        functions->SetObjectArrayElement(this,array,index,val);
    }

    JNI_FORCEINLINE jbooleanArray NewBooleanArray(jsize len) {
        return functions->NewBooleanArray(this,len);
    }
    JNI_FORCEINLINE jbyteArray NewByteArray(jsize len) {
        return functions->NewByteArray(this,len);
    }
    JNI_FORCEINLINE jcharArray NewCharArray(jsize len) {
        return functions->NewCharArray(this,len);
    }
    JNI_FORCEINLINE jshortArray NewShortArray(jsize len) {
        return functions->NewShortArray(this,len);
    }
    JNI_FORCEINLINE jintArray NewIntArray(jsize len) {
        return functions->NewIntArray(this,len);
    }
    JNI_FORCEINLINE jlongArray NewLongArray(jsize len) {
        return functions->NewLongArray(this,len);
    }
    JNI_FORCEINLINE jfloatArray NewFloatArray(jsize len) {
        return functions->NewFloatArray(this,len);
    }
    JNI_FORCEINLINE jdoubleArray NewDoubleArray(jsize len) {
        return functions->NewDoubleArray(this,len);
    }

    JNI_FORCEINLINE jboolean * GetBooleanArrayElements(jbooleanArray array, jboolean *isCopy) {
        return functions->GetBooleanArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jbyte * GetByteArrayElements(jbyteArray array, jboolean *isCopy) {
        return functions->GetByteArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jchar * GetCharArrayElements(jcharArray array, jboolean *isCopy) {
        return functions->GetCharArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jshort * GetShortArrayElements(jshortArray array, jboolean *isCopy) {
        return functions->GetShortArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jint * GetIntArrayElements(jintArray array, jboolean *isCopy) {
        return functions->GetIntArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jlong * GetLongArrayElements(jlongArray array, jboolean *isCopy) {
        return functions->GetLongArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jfloat * GetFloatArrayElements(jfloatArray array, jboolean *isCopy) {
        return functions->GetFloatArrayElements(this,array,isCopy);
    }
    JNI_FORCEINLINE jdouble * GetDoubleArrayElements(jdoubleArray array, jboolean *isCopy) {
        return functions->GetDoubleArrayElements(this,array,isCopy);
    }

    JNI_FORCEINLINE void ReleaseBooleanArrayElements(jbooleanArray array,
                                     jboolean *elems,
                                     jint mode) {
        functions->ReleaseBooleanArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseByteArrayElements(jbyteArray array,
                                  jbyte *elems,
                                  jint mode) {
        functions->ReleaseByteArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseCharArrayElements(jcharArray array,
                                  jchar *elems,
                                  jint mode) {
        functions->ReleaseCharArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseShortArrayElements(jshortArray array,
                                   jshort *elems,
                                   jint mode) {
        functions->ReleaseShortArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseIntArrayElements(jintArray array,
                                 jint *elems,
                                 jint mode) {
        functions->ReleaseIntArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseLongArrayElements(jlongArray array,
                                  jlong *elems,
                                  jint mode) {
        functions->ReleaseLongArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseFloatArrayElements(jfloatArray array,
                                   jfloat *elems,
                                   jint mode) {
        functions->ReleaseFloatArrayElements(this,array,elems,mode);
    }
    JNI_FORCEINLINE void ReleaseDoubleArrayElements(jdoubleArray array,
                                    jdouble *elems,
                                    jint mode) {
        functions->ReleaseDoubleArrayElements(this,array,elems,mode);
    }

    JNI_FORCEINLINE void GetBooleanArrayRegion(jbooleanArray array,
                               jsize start, jsize len, jboolean *buf) {
        functions->GetBooleanArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetByteArrayRegion(jbyteArray array,
                            jsize start, jsize len, jbyte *buf) {
        functions->GetByteArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetCharArrayRegion(jcharArray array,
                            jsize start, jsize len, jchar *buf) {
        functions->GetCharArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetShortArrayRegion(jshortArray array,
                             jsize start, jsize len, jshort *buf) {
        functions->GetShortArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetIntArrayRegion(jintArray array,
                           jsize start, jsize len, jint *buf) {
        functions->GetIntArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetLongArrayRegion(jlongArray array,
                            jsize start, jsize len, jlong *buf) {
        functions->GetLongArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetFloatArrayRegion(jfloatArray array,
                             jsize start, jsize len, jfloat *buf) {
        functions->GetFloatArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void GetDoubleArrayRegion(jdoubleArray array,
                              jsize start, jsize len, jdouble *buf) {
        functions->GetDoubleArrayRegion(this,array,start,len,buf);
    }

    JNI_FORCEINLINE void SetBooleanArrayRegion(jbooleanArray array, jsize start, jsize len,
                               const jboolean *buf) {
        functions->SetBooleanArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetByteArrayRegion(jbyteArray array, jsize start, jsize len,
                            const jbyte *buf) {
        functions->SetByteArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetCharArrayRegion(jcharArray array, jsize start, jsize len,
                            const jchar *buf) {
        functions->SetCharArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetShortArrayRegion(jshortArray array, jsize start, jsize len,
                             const jshort *buf) {
        functions->SetShortArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetIntArrayRegion(jintArray array, jsize start, jsize len,
                           const jint *buf) {
        functions->SetIntArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetLongArrayRegion(jlongArray array, jsize start, jsize len,
                            const jlong *buf) {
        functions->SetLongArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetFloatArrayRegion(jfloatArray array, jsize start, jsize len,
                             const jfloat *buf) {
        functions->SetFloatArrayRegion(this,array,start,len,buf);
    }
    JNI_FORCEINLINE void SetDoubleArrayRegion(jdoubleArray array, jsize start, jsize len,
                              const jdouble *buf) {
        functions->SetDoubleArrayRegion(this,array,start,len,buf);
    }

    JNI_FORCEINLINE jint RegisterNatives(jclass clazz, const JNINativeMethod *methods,
                         jint nMethods) {
        return functions->RegisterNatives(this,clazz,methods,nMethods);
    }
    JNI_FORCEINLINE jint UnregisterNatives(jclass clazz) {
        return functions->UnregisterNatives(this,clazz);
    }

    JNI_FORCEINLINE jint MonitorEnter(jobject obj) {
        return functions->MonitorEnter(this,obj);
    }
    JNI_FORCEINLINE jint MonitorExit(jobject obj) {
        return functions->MonitorExit(this,obj);
    }

    JNI_FORCEINLINE jint GetJavaVM(JavaVM **vm) {
        return functions->GetJavaVM(this,vm);
    }

    JNI_FORCEINLINE void GetStringRegion(jstring str, jsize start, jsize len, jchar *buf) {
        functions->GetStringRegion(this,str,start,len,buf);
    }
    JNI_FORCEINLINE void GetStringUTFRegion(jstring str, jsize start, jsize len, char *buf) {
        functions->GetStringUTFRegion(this,str,start,len,buf);
    }

    JNI_FORCEINLINE void * GetPrimitiveArrayCritical(jarray array, jboolean *isCopy) {
        return functions->GetPrimitiveArrayCritical(this,array,isCopy);
    }
    JNI_FORCEINLINE void ReleasePrimitiveArrayCritical(jarray array, void *carray, jint mode) {
        functions->ReleasePrimitiveArrayCritical(this,array,carray,mode);
    }

    JNI_FORCEINLINE const jchar * GetStringCritical(jstring string, jboolean *isCopy) {
        return functions->GetStringCritical(this,string,isCopy);
    }
    JNI_FORCEINLINE void ReleaseStringCritical(jstring string, const jchar *cstring) {
        functions->ReleaseStringCritical(this,string,cstring);
    }

    JNI_FORCEINLINE jweak NewWeakGlobalRef(jobject obj) {
        return functions->NewWeakGlobalRef(this,obj);
    }
    JNI_FORCEINLINE void DeleteWeakGlobalRef(jweak ref) {
        functions->DeleteWeakGlobalRef(this,ref);
    }

    JNI_FORCEINLINE jboolean ExceptionCheck() {
        return functions->ExceptionCheck(this);
    }

    JNI_FORCEINLINE jobject NewDirectByteBuffer(void* address, jlong capacity) {
        return functions->NewDirectByteBuffer(this, address, capacity);
    }
    JNI_FORCEINLINE void* GetDirectBufferAddress(jobject buf) {
        return functions->GetDirectBufferAddress(this, buf);
    }
    JNI_FORCEINLINE jlong GetDirectBufferCapacity(jobject buf) {
        return functions->GetDirectBufferCapacity(this, buf);
    }
    JNI_FORCEINLINE jobjectRefType GetObjectRefType(jobject obj) {
        return functions->GetObjectRefType(this, obj);
    }

    /* Module Features */

    JNI_FORCEINLINE jobject GetModule(jclass clazz) {
        return functions->GetModule(this, clazz);
    }

#endif /* __cplusplus */
};

typedef struct JavaVMOption {
    char *optionString;
    void *extraInfo;
} JavaVMOption;

typedef struct JavaVMInitArgs {
    jint version;

    jint nOptions;
    JavaVMOption *options;
    jboolean ignoreUnrecognized;
} JavaVMInitArgs;

typedef struct JavaVMAttachArgs {
    jint version;

    char *name;
    jobject group;
} JavaVMAttachArgs;

/* These will be VM-specific. */

#define JDK1_2
#define JDK1_4

/* End VM-specific. */

struct JNIInvokeInterface_ {
    void *reserved0;
    void *reserved1;
    void *reserved2;

    jint (JNICALL *DestroyJavaVM)(JavaVM *vm);

    jint (JNICALL *AttachCurrentThread)(JavaVM *vm, void **penv, void *args);

    jint (JNICALL *DetachCurrentThread)(JavaVM *vm);

    jint (JNICALL *GetEnv)(JavaVM *vm, void **penv, jint version);

    jint (JNICALL *AttachCurrentThreadAsDaemon)(JavaVM *vm, void **penv, void *args);
};

struct JavaVM_ {
    const struct JNIInvokeInterface_ *functions;
#ifdef __cplusplus

    JNI_FORCEINLINE jint DestroyJavaVM() {
        return functions->DestroyJavaVM(this);
    }
    JNI_FORCEINLINE jint AttachCurrentThread(void **penv, void *args) {
        return functions->AttachCurrentThread(this, penv, args);
    }
    JNI_FORCEINLINE jint DetachCurrentThread() {
        return functions->DetachCurrentThread(this);
    }

    JNI_FORCEINLINE jint GetEnv(void **penv, jint version) {
        return functions->GetEnv(this, penv, version);
    }
    JNI_FORCEINLINE jint AttachCurrentThreadAsDaemon(void **penv, void *args) {
        return functions->AttachCurrentThreadAsDaemon(this, penv, args);
    }
#endif
};

#ifdef _JNI_IMPLEMENTATION_
#define _JNI_IMPORT_OR_EXPORT_ JNIEXPORT
#else
#define _JNI_IMPORT_OR_EXPORT_ JNIIMPORT
#endif
_JNI_IMPORT_OR_EXPORT_ jint JNICALL
JNI_GetDefaultJavaVMInitArgs(void *args);

_JNI_IMPORT_OR_EXPORT_ jint JNICALL
JNI_CreateJavaVM(JavaVM **pvm, void **penv, void *args);

_JNI_IMPORT_OR_EXPORT_ jint JNICALL
JNI_GetCreatedJavaVMs(JavaVM **, jsize, jsize *);

/* Defined by native libraries. */
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved);

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *vm, void *reserved);

#define JNI_VERSION_1_1 0x00010001
#define JNI_VERSION_1_2 0x00010002
#define JNI_VERSION_1_4 0x00010004
#define JNI_VERSION_1_6 0x00010006
#define JNI_VERSION_1_8 0x00010008
#define JNI_VERSION_9   0x00090000
#define JNI_VERSION_10  0x000a0000

#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */

#endif /* !_JAVASOFT_JNI_H_ */

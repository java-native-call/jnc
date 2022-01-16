/*
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#ifndef _JAVASOFT_JNI_MD_H_
#define _JAVASOFT_JNI_MD_H_

#include <stdint.h> // NOLINT(modernize-deprecated-headers)

#ifdef _WIN32
#ifndef JNIEXPORT
#define JNIEXPORT __declspec(dllexport)
#endif
#define JNIIMPORT __declspec(dllimport)
#define JNICALL __stdcall

#else /* _WIN32 */

#ifndef __has_attribute
#define __has_attribute(x) 0
#endif

#ifndef JNIEXPORT
#if (defined(__GNUC__) && ((__GNUC__ > 4) || (__GNUC__ == 4) && (__GNUC_MINOR__ > 2))) || __has_attribute(visibility)
#ifdef __arm__
#define JNIEXPORT __attribute__((externally_visible,visibility("default")))
#else
#define JNIEXPORT __attribute__((visibility("default")))
#endif
#else
#define JNIEXPORT
#endif
#endif

#ifndef JNIIMPORT
#if (defined(__GNUC__) && ((__GNUC__ > 4) || (__GNUC__ == 4) && (__GNUC_MINOR__ > 2))) || __has_attribute(visibility)
#ifdef __arm__
#define JNIIMPORT __attribute__((externally_visible,visibility("default")))
#else
#define JNIIMPORT __attribute__((visibility("default")))
#endif
#else
#define JNIIMPORT
#endif
#endif

#define JNICALL

#endif /* _WIN32 */

#ifndef JNI_FORCEINLINE
#if defined(_MSC_VER)
#define JNI_FORCEINLINE __forceinline
#elif (__GNUC__ > 3)
/* Clang also defines __GNUC__ (as 4) */
#define JNI_FORCEINLINE inline __attribute__ ((__always_inline__))
#else
#define JNI_FORCEINLINE inline
#endif
#endif /* JNI_FORCEINLINE */

typedef int8_t jbyte;
typedef int32_t jint;
typedef int64_t jlong;

#endif /* !_JAVASOFT_JNI_MD_H_ */

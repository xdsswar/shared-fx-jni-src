/**
 * Created by XDSSWAR on 6/11/2023.
 */

#ifndef FX_JNI_UTILS_H
#define FX_JNI_UTILS_H
#pragma once
#include <windows.h>
#include <jni.h>

/**
 * Obtains the native handle
 * @param hWnd jlong
 * @return HWND
 */
HWND cats_to_HWND(jlong);

/**
 * Obtains the jlong hwnd from a Window object
 * @param window jobject
 * @return  jlong
 */
jlong get_hwnd(JNIEnv*, jobject);

#endif

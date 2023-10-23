/**
 * Created by XDSSWAR on 6/11/2023.
 */
#pragma once
#include "utils.h"

/**
 * Obtains the native handle
 * @param hWnd jlong
 * @return HWND
 */
HWND cats_to_HWND(jlong hwnd){
    return reinterpret_cast<HWND>(hwnd);
}

/**
 * Obtains the jlong hwnd from a Window object
 * @param window jobject
 * @return  jlong
 */
jlong get_hwnd(JNIEnv* env, jobject window){
    jclass stageClass = env->FindClass("javafx/stage/Window");
    jmethodID getPeerMethod = env->GetMethodID(stageClass, "getPeer", "()Lcom/sun/javafx/tk/TKStage;");
    jclass tkStageClass = env->FindClass("com/sun/javafx/tk/TKStage");
    jmethodID getRawHandleMethod = env->GetMethodID(tkStageClass, "getRawHandle", "()J");
    jobject tkStageObject = env->CallObjectMethod(window, getPeerMethod);
    jlong hwnd = env->CallLongMethod(tkStageObject, getRawHandleMethod);
    return hwnd;
}
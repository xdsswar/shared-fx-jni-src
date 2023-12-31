/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_sun_fx_jni_Decoration */

#ifndef _Included_com_sun_fx_jni_Decoration
#define _Included_com_sun_fx_jni_Decoration
#ifdef __cplusplus
extern "C" {
#endif
#undef com_sun_fx_jni_Decoration_HT_CLIENT
#define com_sun_fx_jni_Decoration_HT_CLIENT 1L
#undef com_sun_fx_jni_Decoration_HT_CAPTION
#define com_sun_fx_jni_Decoration_HT_CAPTION 2L
#undef com_sun_fx_jni_Decoration_HT_SYS_MENU
#define com_sun_fx_jni_Decoration_HT_SYS_MENU 3L
#undef com_sun_fx_jni_Decoration_HT_MIN_BUTTON
#define com_sun_fx_jni_Decoration_HT_MIN_BUTTON 8L
#undef com_sun_fx_jni_Decoration_HT_MAX_BUTTON
#define com_sun_fx_jni_Decoration_HT_MAX_BUTTON 9L
#undef com_sun_fx_jni_Decoration_HT_TOP
#define com_sun_fx_jni_Decoration_HT_TOP 12L
#undef com_sun_fx_jni_Decoration_HT_CLOSE
#define com_sun_fx_jni_Decoration_HT_CLOSE 20L
#undef com_sun_fx_jni_Decoration_DWM_WCP_DEFAULT
#define com_sun_fx_jni_Decoration_DWM_WCP_DEFAULT 0L
#undef com_sun_fx_jni_Decoration_DWM_WCP_DO_NOT_ROUND
#define com_sun_fx_jni_Decoration_DWM_WCP_DO_NOT_ROUND 1L
#undef com_sun_fx_jni_Decoration_DWM_WCP_ROUND
#define com_sun_fx_jni_Decoration_DWM_WCP_ROUND 2L
#undef com_sun_fx_jni_Decoration_DWM_WCP_ROUND_SMALL
#define com_sun_fx_jni_Decoration_DWM_WCP_ROUND_SMALL 3L
/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    getHwNd
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_com_sun_fx_jni_Decoration_getHwNd
  (JNIEnv *, jobject, jobject);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    hideInTaskBar
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_hideInTaskBar
  (JNIEnv *, jobject, jlong);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    showInTaskBar
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_showInTaskBar
  (JNIEnv *, jobject, jlong);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    install
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_install
  (JNIEnv *, jobject, jlong);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    uninstall
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_uninstall
  (JNIEnv *, jobject, jlong);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    update
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_update
  (JNIEnv *, jobject, jlong, jboolean);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    setCornerPreference
 * Signature: (JI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_sun_fx_jni_Decoration_setCornerPreference
  (JNIEnv *, jobject, jlong, jint);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    setBorderColor
 * Signature: (JIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_sun_fx_jni_Decoration_setBorderColor
  (JNIEnv *, jobject, jlong, jint, jint, jint);

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    setBackground
 * Signature: (JIII)V
 */
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_setBackground
  (JNIEnv *, jobject, jlong, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif

/**
 * Created by XDSSWAR on 6/11/2023.
 */

#ifndef FX_JNI_FX_WIN_PROC_H
#define FX_JNI_FX_WIN_PROC_H

#pragma once
#include <windows.h>
#include "HwndMap.h"
#include <jni.h>

#ifndef DWMWA_COLOR_DEFAULT
#define DWMWA_WINDOW_CORNER_PREFERENCE		33
#define DWMWA_BORDER_COLOR					34

typedef enum {
    DWMWCP_DEFAULT = 0,
    DWMWCP_DONOTROUND = 1,
    DWMWCP_ROUND = 2,
    DWMWCP_ROUNDSMALL = 3
} DWM_WINDOW_CORNER_PREFERENCE;


#define DWMWA_COLOR_DEFAULT 0xFFFFFFFF

#define DWMWA_COLOR_NONE    0xFFFFFFFE
#endif

class FXWinProc
{
public:
    static HWND install(JNIEnv* env, jobject obj, HWND hWnd);
    static void uninstall(JNIEnv* env, jobject obj, HWND hwnd);
    static void updateWindow(HWND hwnd, bool max);
    static void setWindowBackground(HWND hwnd, int r, int g, int b);

private:
    static int initialized;
    static jmethodID onNcHitTestMID;
    static jmethodID isFullscreenMID;
    static jmethodID fireStateChangeMID;
    static jmethodID onWmMouseLeaveMID;


    static HwndMap* hwndMap;

    JavaVM* jvm;
    JNIEnv* env;
    jobject obj;
    HWND hwnd;
    WNDPROC defaultWndProc;
    int wmSizeWParam;
    HBRUSH background;
    bool isMovingOrSizing;
    bool isMoving;

    FXWinProc();

    static void initializeJavaCallBacks(JNIEnv* env, jobject obj);

    static LRESULT CALLBACK StaticWindowProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam);

    LRESULT CALLBACK WindowProc(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);

    LRESULT WmDestroy(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);

    LRESULT WmEraseBackground(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);

    LRESULT WmNcCalcSize(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);

    LRESULT WmNcHitTest(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);

    static LRESULT screenToWindowCoordinates(HWND hwnd, LPARAM lParam);

    int getResizeHandleHeight();

    static bool hasAutoHideTaskbar(int edge, RECT rcMonitor);

    BOOL isFullscreen();

    int onNcHitTest(int x, int y, boolean isOnResizeBorder);

    void onWmMouseLeave(HWND hWnd);

    void fireStateChangedLaterOnce();

    JNIEnv* getEnv();

    static void sendMessageToClientArea(HWND hwnd, int uMsg, LPARAM lParam);

    static void openSystemMenu(HWND hwnd, int x, int y);

    static void setMenuItemState(HMENU systemMenu, int item, bool enabled);
};
#endif

/**
 * Created by XDSSWAR on 9/28/2023.
 */
#pragma once
#include <windows.h>
#include <windowsx.h>
#include <dwmapi.h>
#include "utils/utils.h"
#include "utils/HwndMap.h"
#include "utils/FXWinProc.h"
#include "jni_headers/com_sun_fx_jni_Decoration.h"

#pragma comment(lib, "dwmapi.lib")

/**
 * Initialize some stuff
 */
int FXWinProc::initialized = 0;
jmethodID FXWinProc::onNcHitTestMID;
jmethodID FXWinProc::isFullscreenMID;
jmethodID FXWinProc::fireStateChangeMID;
jmethodID FXWinProc::onWmMouseLeaveMID;
HwndMap* FXWinProc::hwndMap;

/**
 * Constructor
 */
FXWinProc::FXWinProc() {
    jvm = nullptr;
    env = nullptr;
    obj = nullptr;
    hwnd = nullptr;
    defaultWndProc = nullptr;
    wmSizeWParam = -1;
    background = nullptr;
    isMovingOrSizing = false;
    isMoving = false;
}

/**
 * Initialize the Java call backs
 * @param env
 * @param obj
 */
void FXWinProc::initializeJavaCallBacks(JNIEnv* env, jobject obj) {
    if (initialized) {
        return;
    }
    initialized = -1;
    jclass cls = env->GetObjectClass(obj);
    onNcHitTestMID = env->GetMethodID(cls, "jniHitTest", "(IIZ)I");
    isFullscreenMID = env->GetMethodID(cls, "jniIsFullScreen", "()Z");
    fireStateChangeMID = env->GetMethodID(cls, "jniFireStateChanged", "()V");
    onWmMouseLeaveMID = env->GetMethodID(cls, "jniInvalidateSpots", "()V");
    if (onNcHitTestMID != nullptr && isFullscreenMID != nullptr && fireStateChangeMID != nullptr
        && onWmMouseLeaveMID != nullptr) {
        initialized = 1;
    }
}

/**
 * Install the FxWinProc
 * @param env
 * @param obj
 * @param window
 * @return
 */
HWND FXWinProc::install(JNIEnv* env, jobject obj, HWND hWnd) {
    initializeJavaCallBacks(env, obj);
    if (initialized < 0)
        return nullptr;

    //create HWND map
    if (hwndMap == nullptr) {
        hwndMap = new HwndMap();
        if (hwndMap == nullptr)
            return nullptr;
    }

    if (hWnd == nullptr || hwndMap->get(hWnd) != nullptr)
        return nullptr;


    auto* wp = new FXWinProc();
    if (!hwndMap->put(hWnd, wp)) {
        delete wp;
        return nullptr;
    }

    env->GetJavaVM(&wp->jvm);
    wp->obj = env->NewGlobalRef(obj);
    wp->hwnd = hWnd;

    // replace window procedure
    wp->defaultWndProc = reinterpret_cast<WNDPROC>(::SetWindowLongPtr(hWnd, GWLP_WNDPROC, (LONG_PTR)FXWinProc::StaticWindowProc));

    return hWnd;
}

/**
 * Uninstall the FXWinProc and restore the old one
 * @param env
 * @param obj
 * @param hwnd
 */
void FXWinProc::uninstall(JNIEnv* env, jobject obj, HWND hwnd) {
    if (hwnd == nullptr)
        return;

    auto* wp = (FXWinProc*)hwndMap->get(hwnd);
    if (wp == nullptr)
        return;

    hwndMap->remove(hwnd);

    // restore original window procedure
    ::SetWindowLongPtr(hwnd, GWLP_WNDPROC, (LONG_PTR)wp->defaultWndProc);

    // show the OS window title bar
    updateWindow(hwnd, 0);

    // cleanup
    env->DeleteGlobalRef(wp->obj);
    if (wp->background != nullptr)
        ::DeleteObject(wp->background);
    delete wp;
}

/**
 * Update the Window
 * @param hwnd
 * @param max
 */
void FXWinProc::updateWindow(HWND hwnd, bool max) {
    auto* wp = (FXWinProc*)hwndMap->get(hwnd);
    if (wp != nullptr) {
        if(max){
            wp->wmSizeWParam = SIZE_MAXIMIZED;
        }else{
            wp->wmSizeWParam = -1;
        }
    }

    // this sends WM_NCCALCSIZE and removes/shows the window title bar
    ::SetWindowPos(hwnd, hwnd, 0, 0, 0, 0,
                   SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);

    if (wp != nullptr)
        wp->wmSizeWParam = -1;
}

/**
 * Set the window bg
 * @param hwnd
 * @param r
 * @param g
 * @param b
 */
void FXWinProc::setWindowBackground(HWND hwnd, int r, int g, int b) {
    auto* wp = (FXWinProc*)hwndMap->get(hwnd);
    if (wp == nullptr)
        return;

    // delete old background brush
    if (wp->background != nullptr)
        ::DeleteObject(wp->background);

    // create new background brush
    wp->background = ::CreateSolidBrush(RGB(r, g, b));
}

/**
 * Create a default WinProc
 * @param hwnd
 * @param uMsg
 * @param wParam
 * @param lParam
 * @return
 */
LRESULT CALLBACK FXWinProc::StaticWindowProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
    auto* wp = (FXWinProc*)hwndMap->get(hwnd);
    if (wp == nullptr)
        return 0;
    return wp->WindowProc(hwnd, uMsg, wParam, lParam);
}

/**
 * Create the custom WinProc for our custom Window
 * @param hWnd
 * @param uMsg
 * @param wParam
 * @param lParam
 * @return
 */
LRESULT CALLBACK FXWinProc::WindowProc(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
    onWmMouseLeave(hWnd);
    switch (uMsg) {
        case WM_NCCALCSIZE:
            return WmNcCalcSize(hWnd, uMsg, wParam, lParam);

        case WM_NCHITTEST:
            return WmNcHitTest(hWnd, uMsg, wParam, lParam);

        case WM_NCMOUSEMOVE:
            // if mouse is moved over some non-client areas,
            // send it also to the client area to allow JavaFx to process it
            // (required for Windows 11 maximize button)
            if (wParam == HTMINBUTTON || wParam == HTMAXBUTTON || wParam == HTCLOSE ||
                wParam == HTCAPTION || wParam == HTSYSMENU) {
                sendMessageToClientArea(hWnd, WM_MOUSEMOVE, lParam);
            }
            break;

        case WM_NCLBUTTONDOWN:
        case WM_NCLBUTTONUP:
            if (wParam == HTMINBUTTON || wParam == HTMAXBUTTON || wParam == HTCLOSE) {
                int uClientMsg = (uMsg == WM_NCLBUTTONDOWN) ? WM_LBUTTONDOWN : WM_LBUTTONUP;
                sendMessageToClientArea(hWnd, uClientMsg, lParam);
                return 0;
            }
            break;

        case WM_NCRBUTTONUP:
            if (wParam == HTCAPTION || wParam == HTSYSMENU)
                openSystemMenu(hWnd, GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
            break;

        case WM_DWMCOLORIZATIONCOLORCHANGED:
            fireStateChangedLaterOnce();
            break;

        case WM_SIZE:
            if (wmSizeWParam >= 0)
                wParam = wmSizeWParam;
            break;

        case WM_ENTERSIZEMOVE:
            isMovingOrSizing = true;
            break;

        case WM_EXITSIZEMOVE:
            isMovingOrSizing = isMoving = false;
            break;

        case WM_MOVE:
        case WM_MOVING:
            if (isMovingOrSizing)
                isMoving = true;
            break;

        case WM_ERASEBKGND:
            if (isMoving)
                return FALSE;

            return WmEraseBackground(hWnd, uMsg, wParam, lParam);

        case WM_DESTROY:
            return WmDestroy(hWnd, uMsg, wParam, lParam);

        default:
            return ::CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam);
    }

    return ::CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam);
}

/**
 * Restore the original Procedure and clean up
 * @param hWnd
 * @param uMsg
 * @param wParam
 * @param lParam
 * @return
 */
LRESULT FXWinProc::WmDestroy(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
    // restore original window procedure
    ::SetWindowLongPtr(hWnd, GWLP_WNDPROC, (LONG_PTR)defaultWndProc);

    WNDPROC defaultWndProc2 = defaultWndProc;

    // cleanup
    getEnv()->DeleteGlobalRef(obj);
    if (background != nullptr)
        ::DeleteObject(background);
    hwndMap->remove(hWnd);
    delete this;

    return ::CallWindowProc(defaultWndProc2, hWnd, uMsg, wParam, lParam);
}

/**
 * Erase bg
 * @param hWnd
 * @param uMsg
 * @param wParam
 * @param lParam
 * @return
 */
LRESULT FXWinProc::WmEraseBackground(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
    if (background == nullptr)
        return FALSE;

    // fill background
    HDC hdc = (HDC)wParam;
    RECT rect;
    ::GetClientRect(hWnd, &rect);
    ::FillRect(hdc, &rect, background);
    return TRUE;
}

/**
 *
 * @param hWnd
 * @param uMsg
 * @param wParam
 * @param lParam
 * @return
 */
LRESULT FXWinProc::WmNcCalcSize(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
    if (wParam != TRUE)
        return ::CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam);

    auto* params = reinterpret_cast<NCCALCSIZE_PARAMS*>(lParam);

    int originalTop = params->rgrc[0].top;


    LRESULT lResult = ::CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam);
    if (lResult != 0)
        return lResult;
    params->rgrc[0].top = originalTop;

    bool isMaximized = ::IsZoomed(hWnd);
    if (isMaximized && !isFullscreen()) {
        params->rgrc[0].top += getResizeHandleHeight();
        APPBARDATA autohide{ 0 };
        autohide.cbSize = sizeof(autohide);
        UINT state = (UINT) ::SHAppBarMessage(ABM_GETSTATE, &autohide);
        if ((state & ABS_AUTOHIDE) != 0) {
            HMONITOR hMonitor = ::MonitorFromWindow(hWnd, MONITOR_DEFAULTTONEAREST);
            MONITORINFO monitorInfo{ 0 };
            ::GetMonitorInfo(hMonitor, &monitorInfo);
            if (hasAutoHideTaskbar(ABE_TOP, monitorInfo.rcMonitor))
                params->rgrc[0].top++;
            if (hasAutoHideTaskbar(ABE_BOTTOM, monitorInfo.rcMonitor))
                params->rgrc[0].bottom--;
            if (hasAutoHideTaskbar(ABE_LEFT, monitorInfo.rcMonitor))
                params->rgrc[0].left++;
            if (hasAutoHideTaskbar(ABE_RIGHT, monitorInfo.rcMonitor))
                params->rgrc[0].right--;
        }
    }

    return lResult;
}

/**
 * Handle hit tests
 * @param hWnd
 * @param uMsg
 * @param wParam
 * @param lParam
 * @return
 */
LRESULT FXWinProc::WmNcHitTest(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam) {
    // this will handle the left, right and bottom parts of the frame because we didn't change them
    LRESULT lResult = ::CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam);
    if (lResult != HTCLIENT)
        return lResult;

    // get mouse x/y in window coordinates
    LRESULT xy = screenToWindowCoordinates(hWnd, lParam);
    int x = GET_X_LPARAM(xy);
    int y = GET_Y_LPARAM(xy);

    int resizeBorderHeight = getResizeHandleHeight();
    bool isOnResizeBorder = (y < resizeBorderHeight) &&
                            (::GetWindowLong(hWnd, GWL_STYLE) & WS_THICKFRAME) != 0;

    return onNcHitTest(x, y, isOnResizeBorder);
}

/**
 * Resize handel height
 * @return int
 */
int FXWinProc::getResizeHandleHeight() {
    UINT dpi = ::GetDpiForWindow(hwnd);

    return ::GetSystemMetricsForDpi(SM_CXPADDEDBORDER, dpi)
           + ::GetSystemMetricsForDpi(SM_CYSIZEFRAME, dpi);
}

/**
 * Check is taskbar is auto-hide
 * @param edge
 * @param rcMonitor
 * @return
 */
bool FXWinProc::hasAutoHideTaskbar(int edge, RECT rcMonitor) {
    APPBARDATA data{ 0 };
    data.cbSize = sizeof(data);
    data.uEdge = edge;
    data.rc = rcMonitor;
    HWND hTaskbar = (HWND) ::SHAppBarMessage(ABM_GETAUTOHIDEBAREX, &data);
    return hTaskbar != nullptr;
}

/**
 * Check if is full screen
 * @return bool
 */
BOOL FXWinProc::isFullscreen() {
    JNIEnv* pJniEnv = getEnv();
    if (pJniEnv == nullptr)
        return FALSE;

    return pJniEnv->CallBooleanMethod(obj, isFullscreenMID);
}

/**
 * Call java side methods on hit tests
 * @param x
 * @param y
 * @param isOnResizeBorder
 * @return
 */
int FXWinProc::onNcHitTest(int x, int y, boolean isOnResizeBorder) {
    JNIEnv* pJniEnv = getEnv();
    if (pJniEnv == nullptr)
        return isOnResizeBorder ? HTTOP : HTCLIENT;

    return pJniEnv->CallIntMethod(obj, onNcHitTestMID, (jint)x, (jint)y, (jboolean)isOnResizeBorder);
}

/**
 * Handle mouse leave
 * @param hWnd
 */
void FXWinProc::onWmMouseLeave(HWND hWnd) {
    POINT point;
    GetCursorPos(&point);
    HWND under= WindowFromPoint(point);
    if (under!=hWnd) {
        JNIEnv *pJniEnv = getEnv();
        if (pJniEnv == nullptr) {
            return;
        }
        pJniEnv->CallVoidMethod(obj, onWmMouseLeaveMID);
    }
}

/**
 * Fire state changes
 */
void FXWinProc::fireStateChangedLaterOnce() {
    JNIEnv* pJniEnv = getEnv();
    if (pJniEnv == nullptr)
        return;
    pJniEnv->CallVoidMethod(obj, fireStateChangeMID);
}

/**
 * Get jvm env
 * @return
 */
JNIEnv* FXWinProc::getEnv() {
    if (env != nullptr)
        return env;

    jvm->GetEnv((void**)&env, JNI_VERSION_1_2);
    return env;
}

/**
 * Send a message to the client area
 * @param hwnd
 * @param uMsg
 * @param lParam
 */
void FXWinProc::sendMessageToClientArea(HWND hwnd, int uMsg, LPARAM lParam) {
    // get mouse x/y in window coordinates
    LRESULT xy = screenToWindowCoordinates(hwnd, lParam);

    // send message
    ::SendMessage(hwnd, uMsg, 0, xy);
}

/**
 * Convert the mouse point to coordinates
 * @param hwnd
 * @param lParam
 * @return
 */
LRESULT FXWinProc::screenToWindowCoordinates(HWND hwnd, LPARAM lParam) {
    RECT rcWindow;
    ::GetWindowRect(hwnd, &rcWindow);

    int x = GET_X_LPARAM(lParam) - rcWindow.left;
    int y = GET_Y_LPARAM(lParam) - rcWindow.top;

    return MAKELONG(x, y);
}

/**
 * Handle sys menu
 * @param hwnd
 * @param x
 * @param y
 */
void FXWinProc::openSystemMenu(HWND hwnd, int x, int y) {
    HMENU systemMenu = ::GetSystemMenu(hwnd, false);

    LONG style = ::GetWindowLong(hwnd, GWL_STYLE);
    bool isMaximized = ::IsZoomed(hwnd);
    setMenuItemState(systemMenu, SC_RESTORE, isMaximized);
    setMenuItemState(systemMenu, SC_MOVE, !isMaximized);
    setMenuItemState(systemMenu, SC_SIZE, (style & WS_THICKFRAME) != 0 && !isMaximized);
    setMenuItemState(systemMenu, SC_MINIMIZE, (style & WS_MINIMIZEBOX) != 0);
    setMenuItemState(systemMenu, SC_MAXIMIZE, (style & WS_MAXIMIZEBOX) != 0 && !isMaximized);
    setMenuItemState(systemMenu, SC_CLOSE, true);

    ::SetMenuDefaultItem(systemMenu, SC_CLOSE, 0);

    int ret = ::TrackPopupMenu(systemMenu, TPM_RETURNCMD, x, y, 0, hwnd, nullptr);
    if (ret != 0)
        ::PostMessage(hwnd, WM_SYSCOMMAND, ret, 0);
}

/**
 * Menu items creation
 * @param systemMenu
 * @param item
 * @param enabled
 */
void FXWinProc::setMenuItemState(HMENU systemMenu, int item, bool enabled) {
    MENUITEMINFO mii{ 0 };
    mii.cbSize = sizeof(mii);
    mii.fMask = MIIM_STATE;
    mii.fType = MFT_STRING;
    mii.fState = enabled ? MF_ENABLED : MF_DISABLED;
    ::SetMenuItemInfo(systemMenu, item, FALSE, &mii);
}



/**
 * =====================================================================================================================
 *
 *                                            JNI STUFF
 *
 * =====================================================================================================================
 */

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    getHwNd
 * Signature: (Ljava/lang/Object;)J
 */
extern "C"
JNIEXPORT jlong JNICALL Java_com_sun_fx_jni_Decoration_getHwNd(JNIEnv *env, jobject obj, jobject window){
    return get_hwnd(env, window);
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    hideInTaskBar
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_hideInTaskBar(JNIEnv *env, jobject obj, jlong hWnd){
    HWND hwnd= cats_to_HWND(hWnd);
    DWORD exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
    exStyle |= WS_EX_TOOLWINDOW;
    SetWindowLong(hwnd, GWL_EXSTYLE, (LONG)exStyle);
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    showInTaskBar
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_showInTaskBar (JNIEnv *env, jobject obj, jlong hWnd){
    HWND hwnd = cats_to_HWND(hWnd);
    DWORD exStyle = GetWindowLong(hwnd, GWL_EXSTYLE);
    exStyle &= ~WS_EX_TOOLWINDOW;
    SetWindowLong(hwnd, GWL_EXSTYLE, (LONG)exStyle);
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    install
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_install(JNIEnv *env, jobject obj, jlong hWnd){
    FXWinProc::install(env,obj, cats_to_HWND(hWnd));
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    uninstall
 * Signature: (J)V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_uninstall(JNIEnv *env, jobject obj, jlong hWnd){
    FXWinProc::uninstall(env,obj, cats_to_HWND(hWnd));
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    update
 * Signature: (JZ)V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_update(JNIEnv *env, jobject obj, jlong hWnd, jboolean update){
    FXWinProc::updateWindow(cats_to_HWND(hWnd), update);
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    setCornerPreference
 * Signature: (JI)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_com_sun_fx_jni_Decoration_setCornerPreference(JNIEnv *env, jobject obj, jlong hWnd, jint pref){
    if (hWnd == 0)
        return FALSE;

    auto attr = (DWM_WINDOW_CORNER_PREFERENCE)pref;
    return ::DwmSetWindowAttribute(
            cats_to_HWND(hWnd),
            DWMWA_WINDOW_CORNER_PREFERENCE,
            &attr, sizeof(attr)) == S_OK;
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    setBorderColor
 * Signature: (JIII)Z
 */
extern "C"
JNIEXPORT jboolean JNICALL Java_com_sun_fx_jni_Decoration_setBorderColor (JNIEnv *env, jobject obj, jlong hWnd, jint red, jint green, jint blue){
    if (hWnd == 0)
        return FALSE;

    COLORREF attr;
    if (red == -1)
        attr = DWMWA_COLOR_DEFAULT;
    else if (red == -2)
        attr = DWMWA_COLOR_NONE;
    else
        attr = RGB(red, green, blue);
    return ::DwmSetWindowAttribute(
            cats_to_HWND(hWnd),
            DWMWA_BORDER_COLOR,
            &attr,
            sizeof(attr)) == S_OK;
}

/**
 * Class:     com_sun_fx_jni_Decoration
 * Method:    setBackground
 * Signature: (JIII)V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_sun_fx_jni_Decoration_setBackground(JNIEnv *env, jobject obj, jlong hWnd, jint red, jint green, jint blue){
    FXWinProc::setWindowBackground(cats_to_HWND(hWnd), red,green, blue);
}
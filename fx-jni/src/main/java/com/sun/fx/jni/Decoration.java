package com.sun.fx.jni;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import xss.it.fx.AbstractDecoration;
import xss.it.fx.helpers.CornerPreference;
import xss.it.fx.helpers.HitSpot;
import xss.it.fx.helpers.State;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author XDSSWAR
 * Created on 09/28/2023
 */
public final class Decoration {
    /**
     * Represents a constant for hit-testing the client area of a window.
     */
    private static final int HT_CLIENT = 1;

    /**
     * Represents a constant for hit-testing the caption/title bar of a window.
     */
    private static final int HT_CAPTION = 2;

    /**
     * Represents a constant for hit-testing the system menu of a window.
     */
    private static final int HT_SYS_MENU = 3;

    /**
     * Represents a constant for hit-testing the minimize button of a window.
     */
    private static final int HT_MIN_BUTTON = 8;

    /**
     * Represents a constant for hit-testing the maximize button of a window.
     */
    private static final int HT_MAX_BUTTON = 9;

    /**
     * Represents a constant for hit-testing the top edge of a window.
     */
    private static final int HT_TOP = 12;

    /**
     * Represents a constant for hit-testing the close button of a window.
     */
    private static final int HT_CLOSE = 20;

    /**
     * Represents a default window composition parameter.
     */
    private static final int DWM_WCP_DEFAULT = 0;

    /**
     * Represents a window composition parameter to avoid rounding.
     */
    private static final int DWM_WCP_DO_NOT_ROUND = 1;

    /**
     * Represents a window composition parameter to round the window.
     */
    private static final int DWM_WCP_ROUND = 2;

    /**
     * Represents a window composition parameter to round the window with a small radius.
     */
    private static final int DWM_WCP_ROUND_SMALL = 3;

    /**
     * Represents the handle of the window.
     */
    private final long hwNd;

    /**
     * EventType for the background change event.
     */
    public static final EventType<WindowEvent> BACKGROUND_CHANGE = new EventType<>(Event.ANY, "BACKGROUND_CHANGE");

    /**
     * Map to store Stage-Decoration pairs for windows.
     */
    private static final Map<Stage, Decoration> STAGE_DECORATION_MAP = Collections.synchronizedMap(new IdentityHashMap<>());

    /**
     * The abstractDecoration associated with this object.
     */
    private final AbstractDecoration abstractDecoration;

    /**
     * The Stage (window) associated with this object.
     */
    private final Stage window;

    /**
     * Prev State
     */
    private State prevState= null;

    /**
     * Constructor for the Decoration class, initializes the window handle.
     *
     * @param window The window object for which decoration is being created.
     * @param abstractDecoration AbstractDecoration
     */
    public Decoration(Stage window, AbstractDecoration abstractDecoration){
        this.window= window;
        this.hwNd = getHwNd(window);
        if (this.hwNd == 0) throw new RuntimeException("Unable to obtain the Stage native handle!");
        this.abstractDecoration = abstractDecoration;

        install(this.hwNd);
        STAGE_DECORATION_MAP.put(window,this);

        update(this.hwNd, window.isMaximized() || window.isFullScreen());

        showInTaskbar(this.abstractDecoration.isShowInTaskBar());
        this.abstractDecoration.showInTaskBarProperty().addListener((obs, o, n) -> {
            showInTaskbar(n);
        });

        if (this.abstractDecoration.getCornerPreference()!=null) {
            setCornerPreference(this.abstractDecoration.getCornerPreference());
        }
        abstractDecoration.cornerPreferenceProperty().addListener((obs, o, preference) -> {
            if (preference!=null) {
                setCornerPreference(preference);
            }
        });

        if (this.abstractDecoration.getWindowBorder()!=null) {
            setBorderColor(this.abstractDecoration.getWindowBorder());
        }
        abstractDecoration.windowBorderProperty().addListener((obs, o, color) -> {
            if (color!=null) {
                setBorderColor(color);
            }
        });

        if (this.abstractDecoration.getWindowBackground()!=null) {
            setBackGroundColor(this.abstractDecoration.getWindowBackground());
        }
        this.abstractDecoration.windowBackgroundProperty().addListener((obs, o, color) -> {
            if (color!=null) {
                setBackGroundColor(color);
            }
        });

        /*
         * Invalidate styles
         */
        window.maximizedProperty().addListener((obs,o, n) -> {
            if (n){
                this.abstractDecoration.setWindowState(State.MAXIMIZED);
            }
            else {
                this.abstractDecoration.setWindowState(State.NORMAL);
            }

        });
        window.fullScreenProperty().addListener((obs,o, n) -> {
            update(this.hwNd,n);
            invalidateSpots();
        });

        window.iconifiedProperty().addListener((obs,o, n) -> {
            if (n){
                this.abstractDecoration.setWindowState(State.MINIMIZED);
            }
            else {
                this.abstractDecoration.setWindowState(prevState!=null ? prevState : State.NORMAL);
            }
        });

        switch (this.abstractDecoration.getWindowState()){
            case NORMAL -> {
                this.window.setMaximized(false);
                this.window.setFullScreen(false);
                this.window.setIconified(false);
            }
            case MINIMIZED -> this.window.setIconified(true);
            case MAXIMIZED -> this.window.setMaximized(true);
        }

        this.abstractDecoration.windowStateProperty().addListener((obs, o, state) -> {
            prevState=o;
            switch (state){
                case NORMAL -> this.window.setMaximized(false);
                case MINIMIZED -> this.window.setIconified(true);
                case MAXIMIZED -> this.window.setMaximized(true);
            }
            update(this.hwNd, state.equals(State.MAXIMIZED) || this.window.isFullScreen());
            invalidateSpots();
        });

        window.widthProperty().addListener((obs,o, n) -> {
            update(this.hwNd, window.isMaximized() || window.isFullScreen());//New update, keep eye
            invalidateSpots();
        });
        window.heightProperty().addListener((obs,o, n) -> {
            update(this.hwNd, window.isMaximized() || window.isFullScreen());//New update, keep eye
            invalidateSpots();
        });
        window.iconifiedProperty().addListener((obs,o, n) -> {
            update(this.hwNd, window.isMaximized() || window.isFullScreen());//New update, keep eye
            invalidateSpots();
        });
    }



    /**
     * Sets the corner preference for the associated object.
     *
     * @param preference The corner preference to set.
     */
    private void setCornerPreference(CornerPreference preference){
        switch (preference){
            case DEFAULT -> setCornerPreference(this.hwNd, DWM_WCP_DEFAULT);
            case NOT_ROUND -> setCornerPreference(this.hwNd, DWM_WCP_DO_NOT_ROUND);
            case ROUND -> setCornerPreference(this.hwNd, DWM_WCP_ROUND);
            case ROUND_SMALL -> setCornerPreference(this.hwNd, DWM_WCP_ROUND_SMALL);
        }
    }

    /**
     * Sets the background color for the associated object.
     *
     * @param color The color to set as the background.
     */
    private void setBackGroundColor(Color color){
        setBackground(this.hwNd, (int) color.getRed(), (int) color.getGreen(), (int) color.getBlue());
    }

    /**
     * Sets the border color for the associated object.
     *
     * @param color The color to set as the border color.
     */
    private void setBorderColor(Color color){
        setBorderColor(this.hwNd, (int) color.getRed(), (int) color.getGreen(), (int) color.getBlue());
    }

    /**
     * Uninstall
     */
    public void uninstall(){
        uninstall(this.hwNd);
        STAGE_DECORATION_MAP.remove(window);
    }

    /**
     * Checks if the given point (x, y) is contained within the specified Rectangle2D object.
     *
     * @param rect The Rectangle2D object to check against.
     * @param x    The x-coordinate of the point.
     * @param y    The y-coordinate of the point.
     * @return True if the point is contained within the rectangle, false otherwise.
     */
    private boolean contains(Rectangle2D rect, int x, int y ) {
        return (rect != null && rect.contains( x, y ) );
    }

    /**
     * Scales down the given Point2D object based on the output scale of the current Screen.
     *
     * @param point The Point2D object to scale down.
     * @return The scaled down Point2D object.
     */
    private Point2D scaleDown(Point2D point) {
        Screen screen= Rect.getCurrentScreen(window);
        double scaleX = screen.getOutputScaleX();
        double scaleY = screen.getOutputScaleY();
        double scaledX = point.getX() / scaleX;
        double scaledY = point.getY() / scaleY;
        return new Point2D(clipRound(scaledX), clipRound(scaledY));
    }

    /**
     * Rounds the given double value and returns it as an integer, clipping it to stay within the range of Integer.MIN_VALUE and Integer.MAX_VALUE.
     *
     * @param value The double value to round.
     * @return The rounded integer value, clipped to the range of Integer.MIN_VALUE and Integer.MAX_VALUE.
     */
    private int clipRound( double value ) {
        value -= 0.5;
        if( value < Integer.MIN_VALUE )
            return Integer.MIN_VALUE;
        if( value > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;
        return (int) Math.ceil( value );
    }

    /**
     * Shows or hides the window in the taskbar.
     *
     * @param show True to show the window in the taskbar, false to hide it.
     */
    public void showInTaskbar(boolean show){
        if (show){
            showInTaskBar(hwNd);
        }
        else {
            hideInTaskBar(hwNd);
        }
    }


    /**
     * Native method to retrieve the window handle (hwNd) for the given window object.
     *
     * @param window The window object.
     * @return The window handle.
     */
    private native long getHwNd(Stage window);

    /**
     * Native method to hide the window with the specified handle in the taskbar.
     *
     * @param hwNd The window handle.
     */
    private native void hideInTaskBar(long hwNd);

    /**
     * Native method to show the window with the specified handle in the taskbar.
     *
     * @param hwNd The window handle.
     */
    private native void showInTaskBar(long hwNd);

    /**
     * Native method to install window decorations for the window with the specified handle.
     *
     * @param hWnd The window handle.
     */
    private native void install(long hWnd);

    /**
     * Native method to uninstall window decorations for the window with the specified handle.
     *
     * @param hWnd The window handle.
     */
    private native void uninstall(long hWnd);

    /**
     * Native method to update window decorations for the window with the specified handle.
     *
     * @param hWnd The window handle.
     * @param maximized True if the window is maximized, false otherwise.
     */
    private native void update(long hWnd, boolean maximized);

    /**
     * Native method to set the corner preference for the window with the specified handle.
     *
     * @param hWnd The window handle.
     * @param pref The corner preference.
     * @return True if the corner preference is set successfully, false otherwise.
     */
    private native boolean setCornerPreference(long hWnd, int pref);

    /**
     * Native method to set the border color for the window with the specified handle.
     *
     * @param hWnd The window handle.
     * @param red   The red component of the border color.
     * @param green The green component of the border color.
     * @param blue  The blue component of the border color.
     */
    private native boolean setBorderColor(long hWnd, int red, int green, int blue);

    /**
     * Native method to set the background color for the window with the specified handle.
     *
     * @param hWnd The window handle.
     * @param r    The red component of the background color.
     * @param g    The green component of the background color.
     * @param b    The blue component of the background color.
     */
    private native void setBackground(long hWnd, int r, int g, int b);



    /*
     * ============================= JNI CALLABLE METHODS ==============================================================
     */

    /**
     * Handles the non-client hit test for the given point (x, y) and resize border flag. Call from JNI
     *
     * @param x                 The x-coordinate of the point.
     * @param y                 The y-coordinate of the point.
     * @param isOnResizeBorder  A boolean flag indicating whether the point is on a resize border.
     * @return The hit test result code.
     */
    private int jniHitTest(int x, int y, boolean isOnResizeBorder ) {
        invalidateSpots();
        /*
         * Scale down mouse x/y because Swing coordinates/values may be scaled on a HiDPI screen.
         */
        Point2D pt = scaleDown(new Point2D(x,y));
        int sx = (int) pt.getX();
        int sy = (int) pt.getY();

        boolean isOnTitleBar = (sy < abstractDecoration.getTitleBarHeight());

        for (HitSpot spot : abstractDecoration.getHitSpots()) {
            if (contains(spot.getRect(),sx,sy)){
                if (spot.isSystemMenu()){//system menu
                    spot.setHovered(true);
                    return HT_SYS_MENU;
                }
                else if(spot.isMinimize()){//minimize
                    spot.setHovered(true);
                    return HT_MIN_BUTTON;
                }
                else if (spot.isMaximize()){//maximize
                    spot.setHovered(true);
                    return HT_MAX_BUTTON;
                }
                else if (spot.isClose()){//close
                    spot.setHovered(true);
                    return HT_CLOSE;
                }
                else if (spot.isClient()){//User controls
                    return HT_CLIENT;
                }
                else {//Refresh styles
                    spot.setHovered(false);
                }
            }
            else {//Invalidate all
                invalidateSpots();
            }
        }

        if (isOnTitleBar) {
            return isOnResizeBorder ? HT_TOP : HT_CAPTION;
        }
        return isOnResizeBorder ? HT_TOP : HT_CLIENT;
    }

    /**
     * Invalidates the hit spots by setting their hover state to false.
     */
    private void invalidateSpots(){
        abstractDecoration.getHitSpots().forEach(hitSpot -> hitSpot.setHovered(false));
    }

    /**
     * Checks if the window is in fullscreen mode. Call from JNI
     *
     * @return True if the window is in fullscreen mode, false otherwise.
     */
    private boolean jniIsFullScreen(){
        return this.window.isFullScreen();
    }

    /**
     * Fires a state change event. Call from JNI.
     * This method triggers the firing of a custom event to indicate a state change.
     */
    private void jniFireStateChanged(){
        window.fireEvent(new Event(BACKGROUND_CHANGE));
    }


    /**
     * Invalidates the hit spots by calling the invalidateSpots() method.
     * This method is invoked from the native side.
     */
    private void jniInvalidateSpots(){
        invalidateSpots();
    }

}

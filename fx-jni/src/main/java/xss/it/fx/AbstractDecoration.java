package xss.it.fx;

import com.sun.fx.jni.Decoration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import xss.it.fx.helpers.CornerPreference;
import xss.it.fx.helpers.HitSpot;
import xss.it.fx.helpers.State;

import java.io.*;
import java.util.List;

/**
 * @author XDSSWAR
 * Created on 09/28/2023
 */
public abstract class AbstractDecoration extends AnchorPane {
    /**
     * Version
     */
    private static final String VERSION = "1.0.0";

    /**
     * The stage associated with this object.
     */
    protected final Stage stage;

    /**
     * The decoration settings for the stage.
     */
    protected Decoration decoration;

    /**
     * Property indicating whether the window should be shown in the taskbar.
     */
    private BooleanProperty showInTaskBar;

    /**
     * Property representing the corner preference for window corners.
     */
    private ObjectProperty<CornerPreference> cornerPreference;

    /**
     * Property representing the window's border color.
     */
    private ObjectProperty<Color> windowBorder;

    /**
     * Property representing the window's background color.
     */
    private ObjectProperty<Color> windowBackground;

    /**
     * Property representing the state of the window.
     */
    private ObjectProperty<State> windowState;

    /**
     * IntegerProperty representing the visibility delay in milliseconds.
     */
    private IntegerProperty visibilityDelay;


    /**
     * Constructs a AbstractDecoration object associated with a Stage and sets the initial showInTaskBar value.
     *
     * @param stage         The Stage associated with the AbstractDecoration.
     * @param showInTaskBar Initial value indicating whether the window should be shown in the taskbar.
     */
    public AbstractDecoration(Stage stage, boolean showInTaskBar){
        this.stage= stage;
        this.setShowInTaskBar(showInTaskBar);
        initialize();
    }

    /**
     * Initialize
     */
    private void initialize(){
        if (this.stage.isShowing()){
            configure();
        }
        else {
            this.stage.setOpacity(0d);
        }
        this.stage.setOnShown(event -> configure());
    }

    /**
     * Configure the Window initial settings
     */
    private void configure(){
        this.decoration = new Decoration(this.stage, this);
        this.decoration.showInTaskbar(isShowInTaskBar());
        show();
    }

    /**
     * Fade in the stage to hide a flick in case happens
     */
    private void show(){
        Timeline timeline = new Timeline();
        KeyFrame key = new KeyFrame(
                Duration.millis(getVisibilityDelay()),
                new KeyValue(this.stage.opacityProperty(),1.0)
        );
        timeline.getKeyFrames().add(key);
        timeline.play();
    }

    /**
     * Returns the BooleanProperty representing whether the window should be shown in the taskbar.
     * If not already initialized, it creates a new BooleanProperty with a default value of true.
     *
     * @return The BooleanProperty for showInTaskBar.
     */
    public final BooleanProperty showInTaskBarProperty() {
        if (showInTaskBar == null) {
            showInTaskBar = new SimpleBooleanProperty(this, "showInTaskBar", true);
        }
        return showInTaskBar;
    }

    /**
     * Checks if the window should be shown in the taskbar.
     *
     * @return True if the window should be shown in the taskbar, false otherwise.
     */
    public final boolean isShowInTaskBar() {
        return showInTaskBarProperty().get();
    }

    /**
     * Sets whether the window should be shown in the taskbar.
     *
     * @param showInTaskBar True to show the window in the taskbar, false to hide it.
     */
    public final void setShowInTaskBar(boolean showInTaskBar) {
        this.showInTaskBarProperty().set(showInTaskBar);
    }

    /**
     * Returns the ObjectProperty representing the corner preference for window corners.
     * If not already initialized, it creates a new ObjectProperty with a default value of CornerPreference.DEFAULT.
     *
     * @return The ObjectProperty for cornerPreference.
     */
    public final ObjectProperty<CornerPreference> cornerPreferenceProperty() {
        if (cornerPreference == null) {
            cornerPreference = new SimpleObjectProperty<>(this, "cornerPreference", CornerPreference.DEFAULT);
        }
        return cornerPreference;
    }

    /**
     * Gets the corner preference for window corners.
     *
     * @return The corner preference.
     */
    public final CornerPreference getCornerPreference() {
        return cornerPreferenceProperty().get();
    }

    /**
     * Sets the corner preference for window corners.
     *
     * @param cornerPreference The corner preference to set.
     */
    public final void setCornerPreference(CornerPreference cornerPreference) {
        this.cornerPreferenceProperty().set(cornerPreference);
    }


    /**
     * Returns the ObjectProperty representing the window's border color.
     * If not already initialized, it creates a new ObjectProperty with no default value.
     *
     * @return The ObjectProperty for windowBorder.
     */
    public final ObjectProperty<Color> windowBorderProperty() {
        if (windowBorder == null) {
            windowBorder = new SimpleObjectProperty<>(this, "windowBorder");
        }
        return windowBorder;
    }

    /**
     * Gets the window's border color.
     *
     * @return The window's border color.
     */
    public final Color getWindowBorder() {
        return windowBorderProperty().get();
    }

    /**
     * Sets the window's border color.
     *
     * @param windowBorder The border color to set.
     */
    public final void setWindowBorder(Color windowBorder) {
        this.windowBorderProperty().set(windowBorder);
    }

    /**
     * Returns the ObjectProperty representing the window's background color.
     * If not already initialized, it creates a new ObjectProperty with no default value.
     *
     * @return The ObjectProperty for windowBackground.
     */
    public final ObjectProperty<Color> windowBackgroundProperty() {
        if (windowBackground == null) {
            windowBackground = new SimpleObjectProperty<>(this, "windowBackground");
        }
        return windowBackground;
    }

    /**
     * Gets the window's background color.
     *
     * @return The window's background color.
     */
    public final Color getWindowBackground() {
        return windowBackgroundProperty().get();
    }

    /**
     * Sets the window's background color.
     *
     * @param windowBackground The background color to set.
     */
    public final void setWindowBackground(Color windowBackground) {
        this.windowBackgroundProperty().set(windowBackground);
    }

    /**
     * Returns the ObjectProperty representing the state of the window.
     * If not already initialized, it creates a new ObjectProperty with a default value of State.NORMAL.
     *
     * @return The ObjectProperty for windowState.
     */
    public final ObjectProperty<State> windowStateProperty() {
        if (windowState == null) {
            windowState = new SimpleObjectProperty<>(this, "windowState", State.NORMAL);
        }
        return windowState;
    }

    /**
     * Gets the state of the window.
     *
     * @return The window state.
     */
    public State getWindowState() {
        return windowStateProperty().get();
    }

    /**
     * Sets the state of the window.
     *
     * @param windowState The window state to set.
     */
    public void setWindowState(State windowState) {
        this.windowStateProperty().set(windowState);
    }

    /**
     * Returns the IntegerProperty representing the visibility delay in milliseconds.
     * If not already initialized, it creates a new IntegerProperty with a default value of 200 milliseconds.
     *
     * @return The IntegerProperty for visibilityDelay.
     */
    public final IntegerProperty visibilityDelayProperty() {
        if (visibilityDelay == null) {
            visibilityDelay = new SimpleIntegerProperty(this, "visibilityDelay", 200);
        }
        return visibilityDelay;
    }

    /**
     * Gets the visibility delay in milliseconds.
     *
     * @return The visibility delay.
     */
    public final int getVisibilityDelay() {
        return visibilityDelayProperty().get();
    }

    /**
     * Sets the visibility delay in milliseconds.
     *
     * @param visibilityDelay The visibility delay to set.
     */
    public final void setVisibilityDelay(int visibilityDelay) {
        this.visibilityDelayProperty().set(visibilityDelay);
    }

    /**
     * Removes decoration
     */
    public final void uninstall(){
        this.decoration.uninstall();
    }

    /**
     * Gets the version information.
     *
     * @return The version information.
     */
    public final String getVersion(){
        return VERSION;
    }

    /**
     * Gets a list of HitSpot objects associated with this object.
     *
     * @return A list of HitSpot objects.
     */
    public abstract List<HitSpot> getHitSpots();

    /**
     * Gets the height of the title bar.
     *
     * @return The height of the title bar.
     */
    public abstract double getTitleBarHeight();


    /*
     * =================================================================================================================
     *
     *                                        Export dll utils
     *
     * =================================================================================================================
     */

    /**
     * Name of the library file.
     */
    private static final String libName= String.format("fx-jni-%s.dll", VERSION);

    /**
     * Name of the folder used for jfx-window-helper.
     */
    private static final String folderName=".fx-jni-libs";

    /**
     * File separator for the current platform.
     */
    private static final String _DIR_SEPARATOR = System.getProperty("file.separator");

    /**
     * User's home directory.
     */
    private static final String _USER_DIR = System.getProperty("user.home");

    /**
     * Flag to determine if initialized
     */
    private static boolean initialized= false;


    /**
     * Exports a DLL file from the application's resources to the specified destination path.
     *
     * @param libName   The name of the DLL file.
     * @param destPath  The destination path where the DLL file will be exported.
     * @throws IOException if an I/O error occurs during the export process.
     */
    public static void exportDll(final String libName, String destPath) throws IOException {
        File file=new File(destPath);
        if (file.exists()) {
            return;
        }
        try (InputStream inputStream = AbstractDecoration.class.getResourceAsStream(String.format("/lib/%s",libName));
             OutputStream outputStream = new FileOutputStream(destPath)) {
            if (inputStream!=null) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }


    /**
     * Create multiple folders inside a specific directory
     * @param folder String folder name to create
     * @return String
     */
    public static String createDirs(String folder) {
        File baseDir = new File(_USER_DIR);
        File f = new File(baseDir, folder);
        if (!f.exists()) {
            boolean ignored= f.mkdirs();
        }
        return f.getAbsolutePath()+_DIR_SEPARATOR;
    }


    /**
     * Initializes the jnilib library.
     */
    private static void init() throws IOException {
        if (!initialized) {
            final String lib = String.format("%s%s", createDirs(folderName), libName);
            exportDll(libName, lib);
            System.load(lib);
            initialized = true;
        }
    }

    /*
     * Initialize and load the Jni
     */
    static {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

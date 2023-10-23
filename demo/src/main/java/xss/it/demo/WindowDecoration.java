package xss.it.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import xss.it.fx.AbstractDecoration;
import xss.it.fx.helpers.HitSpot;
import xss.it.fx.helpers.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XDSSWAR
 * Created on 09/30/2023
 */
public class WindowDecoration extends AbstractDecoration {
    /**
     * The header HBox container for the top section of the UI.
     */
    private final HBox header;

    /**
     * The HBox container for the left section of the header.
     */
    private final HBox hLeft;

    /**
     * An ImageView used to display an icon in the header.
     */
    private final ImageView icon;

    /**
     * A Label used to display the title in the header.
     */
    private final Label title;

    /**
     * The HBox container for the right section of the header.
     */
    private final HBox hRight;

    /**
     * A Button used for minimizing the window.
     */
    private final Button minBtn;

    /**
     * An SVGPath representing the icon for minimizing the window.
     */
    private final SVGPath minSvg;

    /**
     * A Button used for maximizing the window.
     */
    private final Button maxBtn;

    /**
     * An SVGPath representing the icon for maximizing the window.
     */
    private final SVGPath maxSvg;

    /**
     * A Button used for closing the window.
     */
    private final Button closeBtn;

    /**
     * An SVGPath representing the icon for closing the window.
     */
    private final SVGPath closeSvg;

    /**
     * An AnchorPane that serves as the main content container.
     */
    private final AnchorPane container;

    /**
     * A list of HitSpot objects, used for tracking interaction spots.
     */
    private final List<HitSpot> spots;

    /**
     * Constructor for the WindowDecoration class.
     *
     * @param stage The JavaFX Stage object associated with the window.
     * @param showInTaskBar Indicates whether the window should be shown in the taskbar.
     */
    public WindowDecoration(Stage stage, boolean showInTaskBar) {
        // Call the constructor of the parent class (likely a custom class that extends from AnchorPane)
        // and pass the provided Stage and showInTaskBar flag to it.
        super(stage, showInTaskBar);
        header = new HBox();
        hLeft = new HBox();
        icon = new ImageView();
        title = new Label();
        hRight = new HBox();
        minBtn = new Button();
        minSvg = new SVGPath();
        maxBtn = new Button();
        maxSvg = new SVGPath();
        closeBtn = new Button();
        closeSvg = new SVGPath();
        container = new AnchorPane();
        spots = new ArrayList<>();

        // Call the initialize() method to further set up the UI elements
        initialize();
    }

    /**
     * Initialize the UI elements and their properties.
     */
    private void initialize() {
        // Set the base style class for this element.
        getStyleClass().add("base");

        // Configure the header AnchorPane.
        AnchorPane.setLeftAnchor(header, 0.0);
        AnchorPane.setRightAnchor(header, 0.0);
        AnchorPane.setTopAnchor(header, 0.0);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        header.setPrefHeight(40.0);
        header.setPrefWidth(200.0);

        // Configure the left section of the header.
        hLeft.setAlignment(javafx.geometry.Pos.CENTER);
        hLeft.setPrefHeight(100.0);
        hLeft.setPrefWidth(200.0);

        // Configure the icon ImageView.
        icon.setFitHeight(26.0);
        icon.setFitWidth(26.0);
        icon.setPickOnBounds(true);
        icon.setPreserveRatio(true);

        // Set the icon image if available from the stage.
        if (!stage.getIcons().isEmpty()) {
            icon.setImage(stage.getIcons().get(stage.getIcons().size() - 1));
        }

        // Margin for the left section of the header.
        HBox.setMargin(hLeft, new Insets(0.0, 0.0, 5.0, 5.0));

        // Configure the title Label.
        title.setPrefWidth(20000.0);

        title.getStyleClass().add("title");
        title.textProperty().bind(stage.titleProperty());
        HBox.setMargin(title, new Insets(0.0, 10.0, 5.0, 10.0));

        // Configure the right section of the header.
        hRight.setAlignment(Pos.TOP_RIGHT);
        hRight.setPrefHeight(100.0);
        hRight.setPrefWidth(200.0);

        // Configure the minimize button.
        minBtn.setMnemonicParsing(false);
        minBtn.getStyleClass().add("action-button");
        minSvg.setContent(MIN_SHAPE);
        minSvg.setSmooth(false);
        minSvg.getStyleClass().add("action-button-shape");
        minSvg.setScaleX(0.8);
        minSvg.setScaleY(0.8);
        minBtn.setGraphic(minSvg);

        // Configure the maximize/restore button based on window state.
        if (getWindowState().equals(State.MAXIMIZED)) {
            maxSvg.setContent(REST_SHAPE);
        } else if (getWindowState().equals(State.NORMAL)) {
            maxSvg.setContent(MAX_SHAPE);
        }
        maxBtn.setMnemonicParsing(false);
        maxBtn.getStyleClass().add("action-button");
        maxSvg.setSmooth(false);
        maxSvg.getStyleClass().add("action-button-shape");
        maxSvg.setScaleX(0.8);
        maxSvg.setScaleY(0.8);
        maxBtn.setGraphic(maxSvg);

        // Configure the close button.
        closeBtn.setMnemonicParsing(false);
        closeBtn.getStyleClass().add("action-button");
        closeBtn.getStyleClass().add("action-button");
        closeSvg.setContent(CLOSE_SHAPE);
        closeSvg.setScaleX(1);
        closeSvg.setScaleY(1);
        closeSvg.setSmooth(false);
        closeSvg.getStyleClass().add("action-button-shape");
        closeBtn.setGraphic(closeSvg);

        // Configure the container AnchorPane.
        AnchorPane.setBottomAnchor(container, 0.0);
        AnchorPane.setLeftAnchor(container, 0.0);
        AnchorPane.setRightAnchor(container, 0.0);
        AnchorPane.setTopAnchor(container, 40.0);
        container.setPrefHeight(200.0);
        container.setPrefWidth(200.0);

        // Add UI elements to their respective containers.
        hLeft.getChildren().add(icon);
        header.getChildren().add(hLeft);
        header.getChildren().add(title);
        hRight.getChildren().add(minBtn);
        hRight.getChildren().add(maxBtn);
        hRight.getChildren().add(closeBtn);
        header.getChildren().add(hRight);
        getChildren().add(header);
        getChildren().add(container);

        // Build the HitSpot elements.
        buildHitSports();

        // Set up event handlers.
        events();
    }


    /**
     * Set up event listeners and handlers for various UI elements.
     */
    private void events() {
        // Listen for changes in the window state (e.g., maximized, minimized).
        windowStateProperty().addListener((observable, oldValue, state) -> {
            // Update the maximize/restore button's icon based on the window state.
            switch (state) {
                case NORMAL -> maxSvg.setContent(MAX_SHAPE); // Set to "maximize" icon.
                case MAXIMIZED -> maxSvg.setContent(REST_SHAPE); // Set to "restore" icon.
            }
        });

        // Set a click event handler for the minimize button.
        minBtn.setOnAction(event -> setWindowState(State.MINIMIZED));

        // Set a click event handler for the maximize/restore button.
        maxBtn.setOnAction(event -> setWindowState(getWindowState() == State.MAXIMIZED ? State.NORMAL : State.MAXIMIZED));

        // Set a click event handler for the close button to close the window.
        closeBtn.setOnAction(event -> stage.close());
    }


    /**
     * Create and configure HitSpot objects for the UI buttons (minimize, maximize/restore, close).
     * These objects define regions where user interactions are detected.
     * This step is very important. css styles must be handled this way only. Do not waste time using PseudoClasses
     */
    private void buildHitSports() {
        // Create and configure a HitSpot for the minimize button.
        HitSpot minSpot = HitSpot.builder()
                .control(minBtn)
                .maximize(false)
                .minimize(true)
                .close(false)
                .systemMenu(false)
                .build();

        // Add a listener to handle hover effects when the mouse enters or exits the HitSpot.
        minSpot.hoveredProperty().addListener((observable, oldValue, hovered) -> {
            if (hovered) {
                minSpot.getControl().getStyleClass().add("action-button-hover");
            } else {
                minSpot.getControl().getStyleClass().remove("action-button-hover");
            }
        });

        // Create and configure a HitSpot for the maximize/restore button.
        HitSpot maxSpot = HitSpot.builder()
                .control(maxBtn)
                .maximize(true)
                .minimize(false)
                .close(false)
                .systemMenu(false)
                .build();

        // Add a listener to handle hover effects when the mouse enters or exits the HitSpot.
        maxSpot.hoveredProperty().addListener((observable, oldValue, hovered) -> {
            if (hovered) {
                maxSpot.getControl().getStyleClass().add("action-button-hover");
            } else {
                maxSpot.getControl().getStyleClass().remove("action-button-hover");
            }
        });

        // Create and configure a HitSpot for the close button.
        HitSpot closeSpot = HitSpot.builder()
                .control(closeBtn)
                .maximize(false)
                .minimize(false)
                .close(true)
                .systemMenu(false)
                .build();

        // Add a listener to handle hover effects when the mouse enters or exits the HitSpot.
        closeSpot.hoveredProperty().addListener((observable, oldValue, hovered) -> {
            if (hovered) {
                closeSpot.getControl().getStyleClass().add("action-button-close-hover");
            } else {
                closeSpot.getControl().getStyleClass().remove("action-button-close-hover");
            }
        });

        // Add all the configured HitSpot objects to the spots list.
        spots.addAll(List.of(minSpot, maxSpot, closeSpot));

        /*
         * NOTE : You can still add new HitSpots to this list later, and they will be accepted
         * by the native side.
         * Example: a client button in the Header area,like settings button next to action buttons
         *
         * HitSpot clientSpot = HitSpot.builder()
                .control(clientBtn)
                .maximize(false)
                .minimize(false)
                .close(false)
                .systemMenu(false)
                .build();
         *
         * Since all flags are false, the button will be handles as if it where located in the Client Area
         */
    }



    /**
     * Returns a list of HitSpot objects representing interaction areas on the window decoration.
     *
     * @return A list of HitSpot objects.
     */
    @Override
    public List<HitSpot> getHitSpots() {
        return spots;
    }

    /**
     * Returns the height of the title bar in the window decoration.
     *
     * @return The height of the title bar.
     */
    @Override
    public double getTitleBarHeight() {
        return header.getHeight();
    }

    /**
     * SVG path data for the "Minimize" button icon.
     */
    public static final String MIN_SHAPE = "M1 7L1 8L14 8L14 7Z";

    /**
     * SVG path data for the "Maximize" button icon.
     */
    public static final String MAX_SHAPE = "M2.5 2 A 0.50005 0.50005 0 0 0 2 2.5L2 13.5 A 0.50005 0.50005 0 0 0 2.5 14L13.5 14 A 0.50005 0.50005 0 0 0 14 13.5L14 2.5 A 0.50005 0.50005 0 0 0 13.5 2L2.5 2 z M 3 3L13 3L13 13L3 13L3 3 z";

    /**
     * SVG path data for the "Restore" button icon (used when window is maximized).
     */
    public static final String REST_SHAPE = "M4.5 2 A 0.50005 0.50005 0 0 0 4 2.5L4 4L2.5 4 A 0.50005 0.50005 0 0 0 2 4.5L2 13.5 A 0.50005 0.50005 0 0 0 2.5 14L11.5 14 A 0.50005 0.50005 0 0 0 12 13.5L12 12L13.5 12 A 0.50005 0.50005 0 0 0 14 11.5L14 2.5 A 0.50005 0.50005 0 0 0 13.5 2L4.5 2 z M 5 3L13 3L13 11L12 11L12 4.5 A 0.50005 0.50005 0 0 0 11.5 4L5 4L5 3 z M 3 5L11 5L11 13L3 13L3 5 z";

    /**
     * SVG path data for the "Close" button icon.
     */
    public static final String CLOSE_SHAPE = "M3.726563 3.023438L3.023438 3.726563L7.292969 8L3.023438 12.269531L3.726563 12.980469L8 8.707031L12.269531 12.980469L12.980469 12.269531L8.707031 8L12.980469 3.726563L12.269531 3.023438L8 7.292969Z";

}

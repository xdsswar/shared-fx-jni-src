package xss.it.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xss.it.fx.helpers.CornerPreference;

import java.util.Objects;

/**
 * @author XDSSWAR
 * Created on 09/30/2023
 */
public class Demo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX's application entry point where the main window is initialized.
     */
    @Override
    public void start(Stage stage) {
        // Load and set the application icon from a resource file.
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/xss/it/demo/code.png")).toExternalForm()));

        // Create a custom window decoration instance for the stage.
        WindowDecoration windowDecoration = new WindowDecoration(stage, true);

        windowDecoration.setPrefSize(1000, 600);

        // Set the corner preference of the window decoration (how the corners are styled).
        windowDecoration.setCornerPreference(CornerPreference.ROUND);

        // Create a new scene and set it as the content of the stage with specified dimensions.
        Scene scene = new Scene(windowDecoration);

        // Load and apply a stylesheet (CSS) to the scene from a resource file.
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/xss/it/demo/style.css")).toExternalForm());

        // Set the created scene as the content of the stage.
        stage.setScene(scene);

        // Set the title of the stage.
        stage.setTitle("Custom JavaFX Stage using C++/JNI");

        // Show the stage, making it visible to the user.
        stage.show();
    }

}

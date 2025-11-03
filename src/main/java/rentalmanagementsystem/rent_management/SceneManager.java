package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage stage;
    private static final String CSS_PATH = "system.css";

    public static void setStage(Stage s){
        stage = s;
    }

    public static void switchScene(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        String css = SceneManager.class.getResource(CSS_PATH).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
    }

    public static FXMLLoader getFXMLLoader(String fxmlFile) {
        return new FXMLLoader(SceneManager.class.getResource(fxmlFile));
    }
}

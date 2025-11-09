package rentalmanagementsystem.rent_management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.switchScene("login.fxml");
        stage.setTitle("The Pavilion");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
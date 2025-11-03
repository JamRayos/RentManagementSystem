package rentalmanagementsystem.rent_management;

public class AlertMessage {
    public static void showAlert(javafx.scene.control.Alert.AlertType type, String title, String message){
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

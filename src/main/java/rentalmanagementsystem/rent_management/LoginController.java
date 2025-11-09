package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private void onLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Tenant tenant = validateLogin(username, password);
        if (tenant != null){
            SessionLogin.setCurrentTenant(tenant);

            SceneManager.switchScene("complaintTenant.fxml");
        } else {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password");
        }
    }

    @FXML private void onLogout() throws IOException {
        SessionLogin.clear();
        SceneManager.switchScene("login.fxml");
    }

    private Tenant validateLogin(String username, String password) {
        String query = "SELECT * FROM tenantAccount WHERE username = ? AND password = ?";

        try(Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                return new Tenant(
                        rs.getInt("tenantAccountId"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("contactNumber"),
                        rs.getInt("unitId")
                );
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}

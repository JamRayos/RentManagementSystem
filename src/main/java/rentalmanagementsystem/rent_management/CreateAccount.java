package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateAccount {
    @FXML private AnchorPane otpPane;
    @FXML private AnchorPane mainPane;

    @FXML private TextField otpField;
    @FXML private Label unitLabel;
    @FXML private Button otpButton;

    @FXML private TextField tenantNameField;
    @FXML private TextField tenantUsernameField;
    @FXML private TextField tenantEmailFailed;
    @FXML private TextField tenantContactField;
    @FXML private TextField tenantPasswordField;
    @FXML private TextField tenantConfirmPasswordField;

    private int linkedUnitId = -1;
    private String linkedUnitName = "";

    @FXML
    private void handleVerifyOTP() {
        String otpCode = otpField.getText().trim();

        if (otpCode.isEmpty()){
            AlertMessage.showAlert(AlertType.ERROR, "Error", "Please enter your OTP code.");
            return;
        }

        String sql = "SELECT unitId, roomNo FROM roomAccount WHERE otp = ?";
        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, otpCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                linkedUnitId = rs.getInt("unitId");
                linkedUnitName = rs.getString("roomNo");

                unitLabel.setText(linkedUnitName);

                otpPane.setVisible(false);
            } else {
                AlertMessage.showAlert(AlertType.ERROR, "Error", "Invalid or expired OTP code");
            }
        } catch (SQLException e){
            AlertMessage.showAlert(AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterTenant() {
        String otpCode = otpField.getText();

        Tenant tenant = new Tenant(
                0,
                tenantNameField.getText(),
                tenantUsernameField.getText(),
                tenantEmailFailed.getText(),
                tenantContactField.getText(),
                0,
                tenantPasswordField.getText(),
                false
        );

        TenantDAO dao = new TenantDAO();
        int newId = dao.registerTenant(tenant, otpField.getText());

        if (newId > 0){
            AlertMessage.showAlert(AlertType.INFORMATION, "Success", "Registration Complete!");
        } else {
            AlertMessage.showAlert(AlertType.ERROR, "Failed", "Unable to complete registration");
        }
    }
}

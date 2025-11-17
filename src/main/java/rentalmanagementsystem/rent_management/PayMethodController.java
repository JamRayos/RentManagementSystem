package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;

public class PayMethodController {

    @FXML AnchorPane drawerPane;
    @FXML Button menuButton;
    private boolean drawerOpen = true;

    @FXML private Label usernameLabel;
    private int tenantId;
    private int unitId;

    @FXML private AnchorPane bankMethod;
    @FXML private AnchorPane eWalletMethod;

    @FXML //for the side drawer
    private void toggleDrawer() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.millis(300));
        slide.setNode(drawerPane);

        if (drawerOpen) {
            slide.setToX(-250);
            drawerOpen = false;
        } else {
            slide.setToX(0);
            drawerOpen = true;
        }
        slide.play();
    }

    @FXML
    public void initialize() throws IOException{
        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        Tenant tenant = SessionLogin.getCurrentTenant();
        if (tenant != null){
            usernameLabel.setText(tenant.getName() + "!");
            tenantId = tenant.getTenantId();
            unitId = tenant.getUnitId();
        } else {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Login Error", "No tent is currently logged in");
            SceneManager.switchScene("login.fxml");
            return;
        }
    }

    @FXML private void helpNSupportButton(ActionEvent event) throws IOException { SceneManager.switchScene("helpNSupportUI.fxml"); }
    @FXML private void complaintsTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("complaintTenant.fxml"); }
    @FXML private void billingTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantBilling.fxml"); }
    @FXML private void apartmentLeaseButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantLeaseUI.fxml"); }
    @FXML private void PaymentHistoryButton(ActionEvent event) throws IOException { SceneManager.switchScene("PaymentHistoryUI.fxml"); }
    @FXML private void contactNAboutButton(ActionEvent event) throws IOException { SceneManager.switchScene("contactNAbout.fxml"); }
    @FXML private void dashboardButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantDashboard.fxml"); }

    @FXML private void logoutButton(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            SceneManager.switchScene("loginPage.fxml");
        } else {
            alert.close();
        }
    }

    // NEW: Navigate to Bank Payment Page
    @FXML private void bankPayMethodButton(ActionEvent event) throws IOException {
        bankMethod.setVisible(true);
    }

    // NEW: Navigate to eWallet Payment Page
    @FXML private void ewalletPayMethodButton(ActionEvent event) throws IOException {
        eWalletMethod.setVisible(true);
    }

    @FXML private void bankPayMethodCloseButton(ActionEvent event) throws IOException {
        bankMethod.setVisible(false);
    }

    // NEW: Navigate to eWallet Payment Page
    @FXML private void ewalletPayMethodCloseButton(ActionEvent event) throws IOException {
        eWalletMethod.setVisible(false);
    }
}

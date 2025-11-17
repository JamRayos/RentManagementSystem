package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class HelpNSupportController {


    @FXML
    AnchorPane drawerPane;
    @FXML
    Button menuButton;
    private boolean drawerOpen = true;
    @FXML
    Label usernameLabel;
    private String userEmail;
    private String username;


    @FXML private Button BasicsBtn;
    @FXML private Button ProfileBtn;
    @FXML private Button PaymentBtn;
    @FXML private Button MaintBtn;
    @FXML private Button LeaAgrBtn;
    @FXML private Button HelpDeskBtn;

    @FXML private Button exit;
    @FXML private Button exit1;
    @FXML private Button exit2;
    @FXML private Button exit3;
    @FXML private Button exit4;
    @FXML private Button exit5;

    @FXML private Pane payment;
    @FXML private Pane leaseAgreement;
    @FXML private Pane Basic;
    @FXML private Pane Profile;
    @FXML private Pane Maintenance;
    @FXML private Pane helpDesk;



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
    public void initialize() {

        //navdrawer initialization
        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        BasicsBtn.setOnAction(e -> showPane(Basic));
        ProfileBtn.setOnAction(e -> showPane(Profile));
        PaymentBtn.setOnAction(e -> showPane(payment));
        MaintBtn.setOnAction(e -> showPane(Maintenance));
        LeaAgrBtn.setOnAction(e -> showPane(leaseAgreement));
        HelpDeskBtn.setOnAction(e -> showPane(helpDesk));

        BasicsBtn.setOnAction(e -> showPane(Basic));
        ProfileBtn.setOnAction(e -> showPane(Profile));
        PaymentBtn.setOnAction(e -> showPane(payment));
        MaintBtn.setOnAction(e -> showPane(Maintenance));
        LeaAgrBtn.setOnAction(e -> showPane(leaseAgreement));
        HelpDeskBtn.setOnAction(e -> showPane(helpDesk));

        exit.setOnAction(this::handleExit);
        exit1.setOnAction(this::handleExit);
        exit2.setOnAction(this::handleExit);
        exit3.setOnAction(this::handleExit);
        exit4.setOnAction(this::handleExit);
        exit5.setOnAction(this::handleExit);

    }

    @FXML
    private void handleExit(Event event) {
        Basic.setVisible(false);
        Profile.setVisible(false);
        payment.setVisible(false);
        Maintenance.setVisible(false);
        leaseAgreement.setVisible(false);
        helpDesk.setVisible(false);
    }


    private void showPane(Pane targetPane) {
        Basic.setVisible(false);
        Profile.setVisible(false);
        payment.setVisible(false);
        Maintenance.setVisible(false);
        leaseAgreement.setVisible(false);
        helpDesk.setVisible(false);

        targetPane.setVisible(true);
        targetPane.toFront();
    }


    @FXML private void paymentMethodButton(ActionEvent event) throws IOException { SceneManager.switchScene("optionPayMethod.fxml"); }
    @FXML private void complaintsTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("complaintTenant.fxml"); }
    @FXML private void billingTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantBilling.fxml"); }
    @FXML private void apartmentLeaseButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantLeaseUI.fxml"); }
    @FXML private void PaymentHistoryButton(ActionEvent event) throws IOException { SceneManager.switchScene("PaymentHistoryUI.fxml"); }
    @FXML private void contactNAboutButton(ActionEvent event) throws IOException { SceneManager.switchScene("contactNAbout.fxml"); }
    @FXML private void dashboardButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantDashboard.fxml"); }


    @FXML
    private void logoutButton(ActionEvent event) throws IOException {
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

    public void setUserEmail(String email) {
        this.userEmail = email;
        this.username = fetchUsernameFromDatabase(email);

        if (usernameLabel != null) {
            usernameLabel.setText(username + '!');
        }
    }

    private String fetchUsernameFromDatabase(String email) {
        String query = "SELECT username FROM tenantaccount WHERE email = ?";
        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            } else {
                return "User";
            }

        } catch (SQLException e) {
            System.err.println("Error fetching username: " + e.getMessage());
            return "User";
        }
    }
}

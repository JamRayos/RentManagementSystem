package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PaymentHistoryController {
    @FXML private AnchorPane drawerPane;
    private boolean drawerOpen = true;

    @FXML private TableView<PaymentRecord> paymentTable;
    @FXML private TableColumn<PaymentRecord, String> dateColumn;
    @FXML private TableColumn<PaymentRecord, String> amountColumn;
    @FXML private TableColumn<PaymentRecord, String> methodColumn;
    @FXML private Label usernameLabel;

    int tenantId;
    int unitId;


    @FXML private void initialize() {
        try {
            drawerPane.setTranslateX(-350);
            drawerOpen = false;

            setupTableColumns();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Initialization Error", e.getMessage());
        }
        try {
            Tenant tenant = SessionLogin.getCurrentTenant();
            if (tenant != null){
                usernameLabel.setText(tenant.getName() + "!");
                tenantId = tenant.getTenantId();
                unitId = tenant.getUnitId();

                loadPaymentHistory();
            } else {
                AlertMessage.showAlert(Alert.AlertType.ERROR, "Login Error", "No tent is currently logged in");
                SceneManager.switchScene("login.fxml");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cell -> cell.getValue().paymentDateProperty());
        amountColumn.setCellValueFactory(cell -> cell.getValue().amountProperty());
        methodColumn.setCellValueFactory(cell -> cell.getValue().methodProperty());
    }

    private void loadPaymentHistory() {

        String query = "SELECT paymentDate, amountPaid, modeOfPayment " +
                "FROM paymentTracking " +
                "WHERE tenantId = ? " +
                "ORDER BY paymentDate DESC";

        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, tenantId);
            ResultSet rs = stmt.executeQuery();

            paymentTable.getItems().clear();

            while (rs.next()) {
                LocalDate date = rs.getDate("paymentDate").toLocalDate();
                String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                String amount = String.format("₱%.2f", rs.getDouble("amountPaid"));
                String method = rs.getString("modeOfPayment");

                paymentTable.getItems().add(new PaymentRecord(formattedDate, amount, method));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }


    @FXML private void toggleDrawer() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.millis(300));
        slide.setNode(drawerPane);
        slide.setToX(drawerOpen ? -15 : 0);
        drawerOpen = !drawerOpen;
        slide.play();
    }

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PaymentRecord {
        private final javafx.beans.property.SimpleStringProperty paymentDate;
        private final javafx.beans.property.SimpleStringProperty amount;
        private final javafx.beans.property.SimpleStringProperty method;

        public PaymentRecord(String paymentDate, String amount, String method) {
            this.paymentDate = new javafx.beans.property.SimpleStringProperty(paymentDate);
            this.amount = new javafx.beans.property.SimpleStringProperty(amount);
            this.method = new javafx.beans.property.SimpleStringProperty(method);
        }

        public javafx.beans.property.SimpleStringProperty paymentDateProperty() { return paymentDate; }
        public javafx.beans.property.SimpleStringProperty amountProperty() { return amount; }
        public javafx.beans.property.SimpleStringProperty methodProperty() { return method; }
    }

    @FXML private void paymentMethodButton(ActionEvent event) throws IOException { SceneManager.switchScene("optionPayMethod.fxml"); }
    @FXML private void complaintsTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("complaintTenant.fxml"); }
    @FXML private void dashboardTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantDashboard.fxml"); }
    @FXML private void apartmentLeaseButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantLeaseUI.fxml"); }
    @FXML private void billingButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantBilling.fxml"); }
    @FXML private void HelpNSupportButton(ActionEvent event) throws IOException { SceneManager.switchScene("helpNSupportUI.fxml"); }
    @FXML private void ContactNAboutButton(ActionEvent event) throws IOException { SceneManager.switchScene("ContactNAbout.fxml"); }

}

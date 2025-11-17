package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class tenantDashController {
    @FXML private Label dateToday;
    @FXML private Button menuButton;
    @FXML private Label usernameLabel;
    @FXML private Label balance;
    @FXML private Label dueDate;
    @FXML private Label paymentStatus;
    @FXML private TilePane recentPayHistory;

    private int tenantId;
    private int unitId;

    @FXML
    AnchorPane drawerPane;
    @FXML
    Button burger;
    private boolean drawerOpen = true;

    @FXML
    public void initialize() {
        try {
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

            LocalDate today = LocalDate.now();
            dateToday.setText(today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

            loadTenantDashboardData();
            loadRecentPayments(tenantId);

        } catch (Exception e) {
            e.printStackTrace();
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Initialization Error", e.getMessage());
        }
    }

    private double computeBalance(LocalDate startDate, String billingPeriod, int tenantId) throws SQLException {
        if (startDate == null) return 0;

        double periodAmount = 10000;
        LocalDate today = LocalDate.now();
        LocalDate periodStart = startDate;

        long periodDays = switch (billingPeriod.toLowerCase()) {
            case "quarterly" -> 93;
            case "semi-annual" -> 186;
            case "annual" -> 365;
            default -> 31;
        };

        try (Connection conn = DbConn.connectDB()) {
            String query = "SELECT SUM(amountPaid) AS paid FROM paymenttracking " +
                    "WHERE tenantId = ? AND paymentDate >= ? AND paymentDate < ?";

            LocalDate periodEnd = periodStart.plusDays(periodDays);

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));

            ResultSet rs = stmt.executeQuery();
            double paid = 0;
            if (rs.next()) paid = rs.getDouble("paid");

            return paid >= periodAmount ? 0 : periodAmount - paid;
        }
    }

    private LocalDate computeNextDueDate(LocalDate startDate, String billingPeriod, int tenantId) throws SQLException {
        if (startDate == null) return null;

        long periodDays = switch (billingPeriod.toLowerCase()) {
            case "quarterly" -> 93;
            case "semi-annual" -> 186;
            case "annual" -> 365;
            default -> 31;
        };

        LocalDate nextDue = startDate;

        try (Connection conn = DbConn.connectDB()) {
            while (true) {
                String query = "SELECT SUM(amountPaid) AS paid FROM paymenttracking " +
                        "WHERE tenantId = ? AND paymentDate >= ? AND paymentDate < ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, tenantId);
                stmt.setDate(2, java.sql.Date.valueOf(nextDue));
                stmt.setDate(3, java.sql.Date.valueOf(nextDue.plusDays(periodDays)));

                ResultSet rs = stmt.executeQuery();
                double paid = 0;
                if (rs.next()) paid = rs.getDouble("paid");

                if (paid < 10000) break;
                nextDue = nextDue.plusDays(periodDays);
            }
        }

        return nextDue;
    }

    private void loadTenantDashboardData() {
        try {
            LocalDate startDate = null;
            String roomQuery = "SELECT startDate FROM roomaccount WHERE unitId = ?";
            try (Connection conn = DbConn.connectDB();
                 PreparedStatement stmt = conn.prepareStatement(roomQuery)) {
                stmt.setInt(1, unitId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getDate("startDate") != null)
                    startDate = rs.getDate("startDate").toLocalDate();
            }

            String billingPeriod = "Monthly";
            String payStatus = "-";
            String billingQuery = "SELECT billingPeriod, paymentStatus FROM billing WHERE unitId = ?";
            try (Connection conn = DbConn.connectDB();
                 PreparedStatement stmt = conn.prepareStatement(billingQuery)) {
                stmt.setInt(1, unitId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    billingPeriod = rs.getString("billingPeriod");
                    payStatus = rs.getString("paymentStatus");
                }
            }

            double remainingBalance = computeBalance(startDate, billingPeriod, tenantId);
            LocalDate nextDueDate = computeNextDueDate(startDate, billingPeriod, tenantId);

            balance.setText(String.format("₱%.2f", remainingBalance));
            dueDate.setText(nextDueDate != null ? nextDueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—");
            paymentStatus.setText(payStatus != null ? payStatus : "-");

        } catch (Exception e) {
            e.printStackTrace();
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadRecentPayments(int tenantId) {
        recentPayHistory.getChildren().clear();

        String query = "SELECT paymentDate, amountPaid, modeOfPayment " +
                "FROM paymenttracking " +
                "WHERE tenantId = ? " +
                "ORDER BY paymentDate DESC " +
                "LIMIT 2";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, tenantId);
            ResultSet rs = stmt.executeQuery();

            boolean hasPayments = false;

            while (rs.next()) {
                hasPayments = true;

                FXMLLoader loader = new FXMLLoader(getClass().getResource("paymentHistoryCard.fxml"));
                AnchorPane card = loader.load();

                paymentHistoryCardController controller = loader.getController();

                String rawDate = rs.getString("paymentDate");
                LocalDate date = LocalDate.parse(rawDate.substring(0, 10));
                String formattedDate = date.format(formatter);

                controller.setData(
                        formattedDate,
                        rs.getDouble("amountPaid"),
                        rs.getString("modeOfPayment")
                );

                recentPayHistory.getChildren().add(card);
            }

            if (!hasPayments) {
                Label placeholder = new Label("No recent payments.");
                placeholder.setStyle("-fx-font-size: 32px; -fx-text-fill: gray;");
                recentPayHistory.getChildren().add(placeholder);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    @FXML private void paymentMethodButton(ActionEvent event) throws IOException { SceneManager.switchScene("optionPayMethod.fxml"); }
    @FXML private void complaintsTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("complaintTenant.fxml"); }
    @FXML private void billingTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantBilling.fxml"); }
    @FXML private void apartmentLeaseButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantLeaseUI.fxml"); }
    @FXML private void PaymentHistoryButton(ActionEvent event) throws IOException { SceneManager.switchScene("PaymentHistoryUI.fxml"); }
    @FXML private void HelpNSupportButton(ActionEvent event) throws IOException { SceneManager.switchScene("helpNSupportUI.fxml"); }
    @FXML private void ContactNAboutButton(ActionEvent event) throws IOException { SceneManager.switchScene("ContactNAbout.fxml"); }

    @FXML
    private void logoutButton(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionLogin.clear();
            SceneManager.switchScene("firstPg.fxml");
        }
    }
}

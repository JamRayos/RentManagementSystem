package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IO;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private TextField adminUsernameField;
    @FXML private PasswordField adminPasswordField;

    @FXML private void onLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Tenant tenant = validateLogin(username, password);
        if (tenant != null){
            SessionLogin.setCurrentTenant(tenant);
            SceneManager.switchScene("tenantDashboard.fxml");
        } else {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password");
        }
    }

    @FXML private void onAdminLogin() throws IOException {
        String username = adminUsernameField.getText();
        String password = adminPasswordField.getText();

        boolean isAdmin = validateAdminLogin(username, password);
        if (isAdmin) {
            SceneManager.switchScene("dashboardAdmin.fxml");
        } else {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "login Failed", "Invalid username or password");
        }
    }

    @FXML private void toAdminLogin() throws IOException{
        SceneManager.switchScene("adminLogin.fxml");
    }
    @FXML private void toTenantLogin() throws IOException{
        SceneManager.switchScene("login.fxml");
    }

    @FXML private void onLogout() throws IOException {
        SessionLogin.clear();
        SceneManager.switchScene("login.fxml");
    }

    private Tenant validateLogin(String username, String password) {
        String query = "SELECT * FROM tenantAccount WHERE username = ? AND password = ?";

        try (Connection conn = DbConn.connectDB()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                int tenantId = rs.getInt("tenantAccountId");
                int unitId = rs.getInt("unitId");

                Date endDate = getEndDate(conn, unitId);
                Date evictionDate = getEvictionDate(conn, tenantId);

                boolean isExpired = endDate != null &&
                        endDate.toLocalDate().isBefore(LocalDate.now());

                boolean isEvicted = evictionDate != null;

                if (isExpired || isEvicted) {
                    archiveTenantData(conn, tenantId, unitId);
                    return null; // prevent login
                }

                // If still active → login
                return new Tenant(
                        tenantId,
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("contactNumber"),
                        unitId
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void archiveTenantData(Connection conn, int tenantId, int unitId) throws SQLException {

        // 1️⃣ ARCHIVE TENANT ACCOUNT
        String tenantArchiveSQL = """
        INSERT INTO tenantAccountArchive 
        (tenantAccountId, unitId, name, username, email, contactNumber, password, archived, leaseAgreementId, numberOfTenants)
        SELECT tenantAccountId, unitId, name, username, email, contactNumber, password, archived, leaseAgreementId, numberOfTenants
        FROM tenantAccount WHERE tenantAccountId = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(tenantArchiveSQL)) {
            ps.setInt(1, tenantId);
            ps.executeUpdate();
        }

        String payArchiveSQL = """
        INSERT INTO paymentTrackingArchive
        (paymentId, tenantId, modeOfPayment, amountPaid, paymentDate, paymentStatus)
        SELECT paymentTrackingId, tenantId, modeOfPayment, amountPaid, paymentDate, paymentStatus
        FROM paymentTracking WHERE tenantId = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(payArchiveSQL)) {
            ps.setInt(1, tenantId);
            ps.executeUpdate();
        }

        // 3️⃣ ARCHIVE BILLING
        String billingArchiveSQL = """
        INSERT INTO billingArchive
        (billingId, unitId, rentAmount, billingPeriod, paymentStatus, currentBalance, advanceBalance)
        SELECT billingId, unitId, rentAmount, billingPeriod, paymentStatus, currentBalance, advanceBalance
        FROM billing WHERE unitId = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(billingArchiveSQL)) {
            ps.setInt(1, unitId);
            ps.executeUpdate();
        }

        // 4️⃣ ARCHIVE BILLING STATEMENT
        String statementArchiveSQL = """
        INSERT INTO billingStatementArchive
        (billingStatementId, tenantId, totalAmount, rentAmount, waterAmount, maintenanceAmount, damage, waterBillDate, pdfPath, isSent, sendDate)
        SELECT billingStatementId, tenantId, totalAmount, rentAmount, waterAmount, maintenanceAmount, damage, waterBillDate, pdfPath, isSent, sendDate
        FROM billingStatement WHERE tenantId = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(statementArchiveSQL)) {
            ps.setInt(1, tenantId);
            ps.executeUpdate();
        }

        // 5️⃣ DELETE ORIGINAL RECORDS
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM paymentTracking WHERE tenantId = ?")) {
            ps.setInt(1, tenantId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM billing WHERE unitId = ?")) {
            ps.setInt(1, unitId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM billingStatement WHERE tenantId = ?")) {
            ps.setInt(1, tenantId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tenantAccount WHERE tenantAccountId = ?")) {
            ps.setInt(1, tenantId);
            ps.executeUpdate();
        }

        // 6️⃣ UPDATE ROOM STATUS
        String updateRoom = """
        UPDATE roomAccount 
        SET startDate = NULL, endDate = NULL, unitStatus = 'vacant' 
        WHERE unitId = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(updateRoom)) {
            ps.setInt(1, unitId);
            ps.executeUpdate();
        }
    }


    private Date getEvictionDate(Connection conn, int tenantId) throws SQLException {

        String evictQuery = "SELECT evictionDate FROM evictionNotice WHERE tenantId = ? AND status = 'Active'";

        PreparedStatement stmt = conn.prepareStatement(evictQuery);
        stmt.setInt(1, tenantId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDate("evictionDate");
        }

        return null;
    }

    private java.sql.Date getEndDate(Connection conn, int unitId) throws SQLException {
        String roomQuery = "SELECT endDate FROM roomAccount WHERE unitId = ?";
        PreparedStatement stmt = conn.prepareStatement(roomQuery);
        stmt.setInt(1, unitId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDate("endDate");
        }
        return null;
    }

    private boolean validateAdminLogin(String username, String password) {
        String query = "SELECT * FROM adminAccount WHERE username = ? AND password = ?";

        try(Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    @FXML private void toCreateAccount() throws IOException{
        SceneManager.switchScene("createAccount.fxml");
    }
}

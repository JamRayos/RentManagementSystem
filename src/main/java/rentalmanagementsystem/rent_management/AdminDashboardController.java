package rentalmanagementsystem.rent_management;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.animation.TranslateTransition;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import javax.swing.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AdminDashboardController {
    @FXML private Label dashboardDate;
    @FXML private Label noOfOccupied;
    @FXML private Label noOfVacant;
    @FXML private Label noOfOverdue;
    @FXML private Label noOfDue;
    @FXML private Label noOfComplaints;

    @FXML AnchorPane drawerPane;
    @FXML Button burger;
    private boolean drawerOpen = true;

    @FXML
    public void initialize() {

        //navdrawer initialization
        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        dashboardDate.setText(today.format(formatter));

        loadDashboardData();
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

    private void loadDashboardData() {
        loadVacantRoomsCount();
        loadOccupiedRoomsCount();
        loadOverdueCount();
        loadVacantRoomsCount();
        loadComplaintsCount();
        loadDueTodayCount();
    }

    private void loadVacantRoomsCount() {
        try(Connection conn = DbConn.connectDB()) {
            String query = "SELECT COUNT(*) as vacant_count FROM roomAccount WHERE unitStatus = 'vacant'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int vacantCount = rs.getInt("vacant_count");
                noOfVacant.setText(String.valueOf(vacantCount));
            } else {
                noOfVacant.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            noOfVacant.setText("0");
        }
    }

    private void loadOccupiedRoomsCount() {
        try(Connection conn = DbConn.connectDB()) {
            String query = "SELECT COUNT(*) as occupied_count FROM roomAccount WHERE unitStatus = 'occupied'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int occupiedCount = rs.getInt("occupied_count");
                noOfOccupied.setText(String.valueOf(occupiedCount));
            } else {
                noOfOccupied.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            noOfOccupied.setText("0");
        }
    }

    private void loadOverdueCount() {
        int overdueCount = 0;
        try(Connection conn = DbConn.connectDB()) {
            String query = """
                    SELECT r.startDate, b.billingPeriod, b.currentBalance
                    FROM billing b
                    JOIN roomAccount r ON b.unitId = r.unitId
                    JOIN tenantAccount t ON t.unitId = r.unitId
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            LocalDate today = LocalDate.now();

            while (rs.next()){
                double currentBalance = rs.getDouble("currentBalance");
                LocalDate startDate = rs.getDate("startDate").toLocalDate();
                String billingPeriod = rs.getString("billingPeriod");

                if(currentBalance > 0){
                    LocalDate dueDate = calculateDueDate(startDate, billingPeriod);
                    if (today.isAfter(dueDate)){
                        overdueCount++;
                    }
                }
            }

            noOfOverdue.setText(String.valueOf(overdueCount));
        } catch (SQLException e) {
            e.printStackTrace();
            noOfOverdue.setText("0");
        }
    }

    private void loadComplaintsCount() {
        try(Connection conn = DbConn.connectDB()) {
            String query = "SELECT COUNT(*) as complaints_count FROM complaints ";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int dueCount = rs.getInt("complaints_count");
                noOfComplaints.setText(String.valueOf(dueCount));
            } else {
                noOfComplaints.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            noOfComplaints.setText("0");
        }
    }

    private void loadDueTodayCount(){
        int dueCount = 0;

        try(Connection conn = DbConn.connectDB()){
            String query = """
                    SELECT r.startDate, b.billingPeriod, b.currentBalance
                    FROM billing b 
                    JOIN roomAccount r ON b.unitId = r.unitId
                    JOIN tenantAccount t ON t.unitId = r.unitId
                    """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            LocalDate today = LocalDate.now();

            while(rs.next()){
                double currentBalance = rs.getDouble("currentBalance");
                LocalDate startDate = rs.getDate("startDate").toLocalDate();
                String billingPeriod = rs.getString("billingPeriod");

                if (currentBalance > 0){
                    LocalDate dueDate = calculateNextDueDate(startDate, billingPeriod, today);
                    if (today.isEqual(calculateNextDueDate(startDate, billingPeriod, today))){
                        dueCount++;
                    }
                }
            }

            noOfDue.setText(String.valueOf(dueCount));

        } catch (SQLException e) {
            e.printStackTrace();
            noOfDue.setText("0");
        }
    }

    private LocalDate calculateDueDate(LocalDate startDate, String billingPeriod) {
        int monthsToAdd;
        switch(billingPeriod.toLowerCase()){
            case "monthly" -> monthsToAdd = 1;
            case "quarterly" -> monthsToAdd = 3;
            case "semi-annual" -> monthsToAdd = 6;
            case "annual" -> monthsToAdd = 12;
            default -> monthsToAdd = 1;
        }

        LocalDate dueDate = startDate;
        LocalDate today = LocalDate.now();

        while (!dueDate.isAfter(today)){
            dueDate = dueDate.plusMonths(monthsToAdd);
        }

        return dueDate.minusMonths(monthsToAdd);
    }

    private LocalDate calculateNextDueDate(LocalDate startDate, String billingPeriod, LocalDate today) {
        int monthsToAdd;
        switch(billingPeriod.toLowerCase()){
            case "monthly" -> monthsToAdd = 1;
            case "quarterly" -> monthsToAdd = 3;
            case "semi-annual" -> monthsToAdd = 6;
            case "annual" -> monthsToAdd = 12;
            default -> monthsToAdd = 1;
        }

        LocalDate dueDate = startDate;

        while (!dueDate.isAfter(today)){
            dueDate = dueDate.plusMonths(monthsToAdd);
        }

        return dueDate.minusMonths(monthsToAdd);
    }

    @FXML private void dashboard (ActionEvent event) throws IOException {SceneManager.switchScene("dashboardAdmin.fxml");}
    @FXML private void complaints (ActionEvent event) throws IOException {SceneManager.switchScene("adminComplaint.fxml");}
    @FXML private void tenantOverview (ActionEvent event) throws IOException {SceneManager.switchScene("overviewOfTenants.fxml");}
    @FXML private void billing (ActionEvent event) throws IOException {SceneManager.switchScene("billingStatement.fxml");}
    @FXML private void linkAccount (ActionEvent event) throws IOException {SceneManager.switchScene("roomAccount.fxml");}
    @FXML private void paymentTracking (ActionEvent event) throws IOException {SceneManager.switchScene("paymentTracking.fxml");}
    @FXML private void overdue (ActionEvent event) throws IOException {SceneManager.switchScene("overdueTenants.fxml");}
    @FXML private void lease (ActionEvent event) throws IOException {SceneManager.switchScene("leaseManagement.fxml");}

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

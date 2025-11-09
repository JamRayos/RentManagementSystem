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

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        dashboardDate.setText(today.format(formatter));

        loadDashboardData();
    }

    private void loadDashboardData() {
        loadVacantRoomsCount();
        loadOccupiedRoomsCount();
        loadOverdueCount();
        loadVacantRoomsCount();
        loadComplaintsCount();
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
        try(Connection conn = DbConn.connectDB()) {
            String query = "SELECT COUNT(*) as overdue_count FROM paymentTracking WHERE paymentStatus = 'overdue'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int overdueCount = rs.getInt("overdue_count");
                noOfOverdue.setText(String.valueOf(overdueCount));
            } else {
                noOfOverdue.setText("0");
            }
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


    @FXML
    private void tenantOverview (ActionEvent event) throws IOException {
        SceneManager.switchScene("overviewOfTenants.fxml");
    }

    @FXML
    private void complaints (ActionEvent event) throws IOException {
        SceneManager.switchScene("complaintTenant.fxml");
    }

    @FXML
    private void billingStatement (ActionEvent event) throws IOException {
        SceneManager.switchScene("billingStatement.fxml");
    }

    @FXML private void paymentTracking(ActionEvent event) throws IOException {
        SceneManager.switchScene("paymentTracking.fxml");
    }

    @FXML private void overdue(ActionEvent event) throws IOException {
        SceneManager.switchScene("overdueTenants.fxml");
    }

    @FXML private void leaseManagement(ActionEvent event) throws IOException {
        SceneManager.switchScene("leaseManagement.fxml");
    }
}

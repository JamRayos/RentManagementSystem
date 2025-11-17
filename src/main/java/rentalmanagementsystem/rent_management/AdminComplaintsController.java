package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AdminComplaintsController {
    @FXML TilePane complaintTilePane;

    @FXML AnchorPane drawerPane;
    @FXML
    Button burger;
    private boolean drawerOpen = true;

    @FXML public void initialize() {
        //navdrawer initialization
        drawerPane.setTranslateX(-350);
        drawerOpen = false;
        loadComplaints();
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

    private void loadComplaints() {
        complaintTilePane.getChildren().clear();
        String query = """
                SELECT c.*, t.name, t.email FROM complaints c
                JOIN tenantAccount t ON c.tenantId = t.tenantAccountId WHERE c.status = 'pending'
                """;

        try (Connection conn = DbConn.connectDB()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("adminComplaintCard.fxml"));
                AnchorPane card = loader.load();

                Timestamp ts = rs.getTimestamp("createdAt");
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy"); // or "MMM dd, yyyy"
                String dateOnly = sdf.format(ts);

                AdminComplaintCardController controller = loader.getController();
                controller.setData(
                        rs.getInt("complaintId"),
                        rs.getString("name"),
                        rs.getString("title"),
                        rs.getString("description"),
                        dateOnly,
                        rs.getString("email")
                );

                complaintTilePane.getChildren().add(card);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void loadComplaintsFilter(String category) {
        complaintTilePane.getChildren().clear();
        String query = """
                SELECT c.*, t.name, t.email FROM complaints c
                JOIN tenantAccount t ON c.tenantId = t.tenantAccountId WHERE c.status = 'pending'
                """;

        if (category != null && !category.equalsIgnoreCase("All")) {
            query += " AND c.category = ?";
        }

        try (Connection conn = DbConn.connectDB()) {
            PreparedStatement stmt = conn.prepareStatement(query);

            if (category != null && !category.equalsIgnoreCase("All")) {
                stmt.setString(1, category);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("adminComplaintCard.fxml"));
                AnchorPane card = loader.load();

                Timestamp ts = rs.getTimestamp("createdAt");
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                String dateOnly = sdf.format(ts);

                AdminComplaintCardController controller = loader.getController();
                controller.setData(
                        rs.getInt("complaintId"),
                        rs.getString("name"),
                        rs.getString("title"),
                        rs.getString("description"),
                        dateOnly,
                        rs.getString("email")
                );

                complaintTilePane.getChildren().add(card);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

//    buttons for filter

    @FXML private void filterAll(){
        loadComplaintsFilter("All");
    }

    @FXML private void filterElectricity(){
        loadComplaintsFilter("electricity");
    }

    @FXML private void filterWater(){
        loadComplaintsFilter("water");
    }

    @FXML private void filterDamages(){
        loadComplaintsFilter("damages");
    }

    @FXML private void filterOther(){
        loadComplaintsFilter("others");
    }

    @FXML private void dashboard (ActionEvent event) throws IOException {SceneManager.switchScene("dashboardAdmin.fxml");}
    @FXML private void complaints (ActionEvent event) throws IOException {SceneManager.switchScene("adminComplaint.fxml");}
    @FXML private void tenantOverview (ActionEvent event) throws IOException {SceneManager.switchScene("overviewOfTenants.fxml");}
    @FXML private void billing (ActionEvent event) throws IOException {SceneManager.switchScene("billingStatement.fxml");}
    @FXML private void linkAccount (ActionEvent event) throws IOException {SceneManager.switchScene("roomAccount.fxml");}
    @FXML private void paymentTracking (ActionEvent event) throws IOException {SceneManager.switchScene("paymentTracking.fxml");}
    @FXML private void overdue (ActionEvent event) throws IOException {SceneManager.switchScene("overdueTenants.fxml");}
    @FXML private void lease (ActionEvent event) throws IOException {SceneManager.switchScene("leaseManagement.fxml");}
}
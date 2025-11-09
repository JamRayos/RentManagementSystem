package rentalmanagementsystem.rent_management;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AdminComplaintsController {
    @FXML TilePane complaintTilePane;

    @FXML public void initialize() {
        loadComplaints();
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
    private void tenantOverview (ActionEvent event) throws IOException {
        SceneManager.switchScene("OverviewOfTenants.fxml");
    }

    @FXML
    private void dashboard (ActionEvent event) throws IOException {
        SceneManager.switchScene("dashboardAdmin.fxml");
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
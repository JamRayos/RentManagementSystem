package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TenantComplaintController {
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextArea descriptionArea;
    @FXML private TilePane complaintsTilePane;
    @FXML private AnchorPane complaintsPane;

    public void initialize() {
        categoryBox.getItems().addAll("electricity", "water", "damages", "others");
        loadTenantComplaints();
    }

    @FXML private void onSubmitComplaint() {
        String title = titleField.getText();
        String category = categoryBox.getValue();
        String description = descriptionArea.getText();

        if (title.isEmpty() || category == null || description.isEmpty()) {
            AlertMessage.showAlert(Alert.AlertType.WARNING, "Warning", "Please complete all fields");
            return;
        }

        int tenantId = SessionLogin.getCurrentTenant().getTenantId();

        String query = """
                INSERT INTO complaints (tenantId, title, category, description, status, adminReply, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, 'pending', null, NOW(), null)
                """;

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            stmt.setString(2, title);
            stmt.setString(3, category);
            stmt.setString(4, description);
            stmt.executeUpdate();

            AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Success", "complaint successfully created");

            titleField.clear();
            descriptionArea.clear();
            categoryBox.setValue(null);

            complaintsPane.setVisible(false);
            loadTenantComplaints();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadTenantComplaints() {
        complaintsTilePane.getChildren().clear();

        String query = "SELECT * FROM complaints WHERE tenantId = ? ORDER BY createdAt DESC";
        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setInt(1, SessionLogin.getCurrentTenant().getTenantId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tenantComplaintCard.fxml"));
                AnchorPane card = loader.load();

                ComplaintCardController controller = loader.getController();
                controller.setData(
                        rs.getInt("complaintId"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status")
                );
                complaintsTilePane.getChildren().add(card);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML private void showAddComplaint() {
        complaintsPane.setVisible(true);
    }
}

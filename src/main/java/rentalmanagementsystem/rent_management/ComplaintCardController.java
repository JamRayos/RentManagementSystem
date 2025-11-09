package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ComplaintCardController {
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label statusLabel;
    @FXML private Button deleteBtn;
    @FXML private Button editBtn;

    private int complaintId;

    public void setData(int id, String title, String description, String status) {
        this.complaintId = id;
        titleLabel.setText(title);
        descriptionLabel.setText(description);
        statusLabel.setText(status);

        if ("Completed".equalsIgnoreCase(status)) {
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
        }
    }

    @FXML
    private void onDelete() {
        deleteComplaint(complaintId);
    }

    @FXML
    private void onEdit() {
        // to follow hehehehe
    }

    private static void deleteComplaint(int complaintId) {
        String query = "DELETE FROM complaints WHERE complaintId = ?";
        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, complaintId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.sql.*;

public class AdminComplaintCardController {

    @FXML private Label nameComplaint;
    @FXML private Label complaintTitle;
    @FXML private Label complaintDescription;
    @FXML private Label complaintDate;
    @FXML private Button sendReply;
    @FXML private CheckBox checkboxComplaint;

    private int complaintId;
    private String tenantEmail;

    public void setData(int id, String name, String title, String description, String date, String email) {
        this.complaintId = id;
        this.tenantEmail = email;

        nameComplaint.setText(name);
        complaintTitle.setText(title);
        complaintDescription.setText(description);
        complaintDate.setText(date);

        sendReply.setOnAction(e -> openReplyDialog());
    }

    @FXML private void openReplyDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply to Complaint");
        dialog.setHeaderText("Enter your reply to " + nameComplaint.getText());
        dialog.setContentText("Reply:");

        dialog.showAndWait().ifPresent(reply -> {
            sendEmailReply(reply);
            updateComplaintStatus(reply);
        });
    }

    private void sendEmailReply(String replyText) {
        final String from = "brotres091205@gmail.com";
        final String password = "lbynbsxztcuflcfq";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(tenantEmail));
            message.setSubject("Complaint Response");
            message.setText("Dear " + nameComplaint.getText() + ",\n\n" + replyText + "\n\nBest regards,\nAdmin");

            Transport.send(message);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Reply sent successfully!");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to send email: " + e.getMessage());
            alert.show();
        }
    }

    private void updateComplaintStatus(String replyText) {
        String query = "UPDATE complaints SET adminReply = ? WHERE complaintId = ?";

        try (Connection conn = DbConn.connectDB()) {

            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, replyText);
            ps.setInt(2, complaintId);
            ps.executeUpdate();

            checkboxComplaint.setSelected(true);
            sendReply.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCheckboxClicked() {
        if (checkboxComplaint.isSelected()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to mark this complaint as completed?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    markComplaintCompleted();
                } else {
                    checkboxComplaint.setSelected(false);
                }
            });
        }
    }

    private void markComplaintCompleted() {
        String query = "UPDATE complaints SET status = 'completed' WHERE complaintId = ?";

        try (Connection conn = DbConn.connectDB()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, complaintId);
            ps.executeUpdate();

            sendReply.setDisable(true); // disable reply after completion
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Complaint marked as completed!");
            info.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

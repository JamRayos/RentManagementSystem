package rentalmanagementsystem.rent_management;

import com.lowagie.text.Table;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TenantBillingController {
    @FXML
    private TableView<BillingRecord> billingTable;
    @FXML private TableColumn<BillingRecord, String> idColumn;
    @FXML private TableColumn<BillingRecord, String> dateColumn;
    @FXML private TableColumn<BillingRecord, String> amountColumn;

    @FXML
    AnchorPane drawerPane;
    @FXML
    Button burger;
    private boolean drawerOpen = true;

    private int tenantId;

    @FXML public void initialize() {

        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        Tenant tenant = SessionLogin.getCurrentTenant();
        if (tenant == null) {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "No tenant found");
            return;
        }
        tenantId = tenant.getTenantId();

        setupColumn();
        loadBillingHistory();

        billingTable.setRowFactory(tv -> {
            TableRow<BillingRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) { // double-click to open
                    BillingRecord selected = row.getItem();
                    showPreview(selected);
                }
            });
            return row;
        });
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

    private void setupColumn() {
        idColumn.setCellValueFactory(c -> c.getValue().billingStatementIdProperty());
        dateColumn.setCellValueFactory(c -> c.getValue().sendDateProperty());
        amountColumn.setCellValueFactory(c -> c.getValue().totalAmountProperty());
    }

    private void loadBillingHistory() {
        String sql = "SELECT billingStatementId, sendDate, totalAmount, pdfPath FROM billingStatement WHERE tenantId = ? ORDER BY sendDate DESC";

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, tenantId);
            ResultSet rs = stmt.executeQuery();

            billingTable.getItems().clear();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

            while (rs.next()){
                int id = rs.getInt("billingStatementId");
                String date = rs.getDate("sendDate") != null ? rs.getDate("sendDate").toLocalDate().format(fmt) : "Not Sent";
                String total = String.format("₱%.2f", rs.getDouble("totalAmount"));
                String path = rs.getString("pdfPath");

                billingTable.getItems().add(new BillingRecord(String.valueOf(id), date, total, path));
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showPreview(BillingRecord record) {
        if (record == null || record.getPdfPath() == null) {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "No statement selected or no PDF available");
            return;
        }

        File pdfFile = new File(record.getPdfPath());
        if (!pdfFile.exists()) {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "PDF file not found");
            return;
        }

        try (PDDocument doc = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage img = renderer.renderImageWithDPI(0, 120);
            Image fxImg = SwingFXUtils.toFXImage(img, null);

            ImageView view = new ImageView(fxImg);
            view.setFitWidth(600);
            view.setPreserveRatio(true);

            // --- Download button at bottom ---
            Button downloadBtn = new Button("Download PDF");
            downloadBtn.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
        """);

            downloadBtn.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setInitialFileName("BillingStatement_" + record.getStatementId() + ".pdf");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File dest = chooser.showSaveDialog(null);

                if (dest != null) {
                    try {
                        Files.copy(pdfFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Download Complete", "File saved to: " + dest.getAbsolutePath());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Failed to download PDF");
                    }
                }
            });

            // --- Layout ---
            VBox box = new VBox(15,
                    new Label("Preview of Billing Statement #" + record.getStatementId()),
                    view,
                    downloadBtn
            );
            box.setStyle("-fx-padding: 15; -fx-background-color: white;");
            box.setPrefWidth(620);
            box.setPrefHeight(800);
            box.setAlignment(javafx.geometry.Pos.CENTER);

            Stage dialog = new Stage();
            dialog.setTitle("Billing Statement Preview");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(box));
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Preview Error", "Failed to preview PDF.");
        }
    }



    private void handleDownload() {
        BillingRecord record = billingTable.getSelectionModel().getSelectedItem();
        if (record == null || record.getPdfPath() == null) {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Please select a statement first");
            return;
        }

        File source = new File(record.getPdfPath());
        if (!source.exists()) {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "PDF file not found on server");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("BillingStatement_" + record.getStatementId() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File dest = chooser.showSaveDialog(null);
        if (dest == null) return;

        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Download Complete", "File saved to: " + dest.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Failed to download PDF");
        }
    }

    public static class BillingRecord {
        private final SimpleStringProperty billingStatementId;
        private final SimpleStringProperty sendDate;
        private final SimpleStringProperty totalAmount;
        private final String pdfPath;

        public BillingRecord(String billingStatementId, String sendDate, String totalAmount, String pdfPath) {
            this.billingStatementId = new SimpleStringProperty(billingStatementId);
            this.sendDate = new SimpleStringProperty(sendDate);
            this.totalAmount = new SimpleStringProperty(totalAmount);
            this.pdfPath = pdfPath;
        }

        public StringProperty billingStatementIdProperty() { return billingStatementId; }
        public StringProperty sendDateProperty() { return sendDate; }
        public StringProperty totalAmountProperty() { return totalAmount; }
        public String getPdfPath() { return pdfPath; }
        public String getStatementId() { return billingStatementId.get(); }
    }

    @FXML private void paymentMethodButton(ActionEvent event) throws IOException { SceneManager.switchScene("optionPayMethod.fxml"); }
    @FXML private void complaintsTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("complaintTenant.fxml"); }
    @FXML private void dashboardTenantButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantDashboard.fxml"); }
    @FXML private void apartmentLeaseButton(ActionEvent event) throws IOException { SceneManager.switchScene("tenantLeaseUI.fxml"); }
    @FXML private void PaymentHistoryButton(ActionEvent event) throws IOException { SceneManager.switchScene("PaymentHistoryUI.fxml"); }
    @FXML private void HelpNSupportButton(ActionEvent event) throws IOException { SceneManager.switchScene("helpNSupportUI.fxml"); }
    @FXML private void ContactNAboutButton(ActionEvent event) throws IOException { SceneManager.switchScene("ContactNAbout.fxml"); }

    @FXML private void logoutButton(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionLogin.clear();
            SceneManager.switchScene("login.fxml");
        }
    }
}

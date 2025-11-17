package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Random;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.PDFRenderer;


public class BillingStatementController {

    @FXML AnchorPane drawerPane;
    @FXML Button burger;
    private boolean drawerOpen = true;

    @FXML private TextField nameField;
    @FXML private TextField maintenanceField;
    @FXML private TextArea damageField;
    @FXML private Label roomLabel;
    @FXML private Label phoneLabel;
    @FXML private DatePicker maintenanceDate;

    private final ContextMenu suggestionsMenu = new ContextMenu();
    private int tenantId;
    private int unitId;

    @FXML public void initialize() {

        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        nameField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String query = nameField.getText().trim();
            if (!query.isEmpty()) {
                showTenantSuggestions(query);
            } else {
                suggestionsMenu.hide();
            }
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

    private void showTenantSuggestions(String query){
        suggestionsMenu.getItems().clear();

        String sql = """
                SELECT t.tenantAccountId, t.name, t.unitId, t.contactNumber, r.roomNo FROM tenantAccount t
                JOIN roomAccount r ON t.unitId = r.unitId WHERE LOWER(t.name) LIKE LOWER(?) LIMIT 5
                """;

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();

            boolean hasResults = false;

            while (rs.next()){
                hasResults = true;
                int id = rs.getInt("tenantAccountId");
                int unit = rs.getInt("unitId");
                String name = rs.getString("name");
                String roomNo = rs.getString("roomNo");
                String phone = rs.getString("contactNumber");

                MenuItem item = new MenuItem(name + " - Room " + roomNo);
                item.setOnAction(e -> {
                    tenantId = id;
                    unitId = unit;
                    nameField.setText(name);
                    roomLabel.setText(roomNo);
                    phoneLabel.setText(phone);
                    suggestionsMenu.hide();
                });
                suggestionsMenu.getItems().add(item);

            }

            if (hasResults) {
                suggestionsMenu.show(nameField, Side.BOTTOM, 0, 0);
            } else {
                suggestionsMenu.hide();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleGeneratePDF() {
        if (tenantId == 0){
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Please select a tenant first");
            return;
        }

        double maintenanceAmount = maintenanceField.getText().isEmpty() ? 0 :
                Double.parseDouble(maintenanceField.getText());
        String damage = damageField.getText().isEmpty() ? "none" : damageField.getText();

        double waterAmount = new Random().nextInt(200) + 100;

        RentInfo rentInfo = getRentInfo(unitId);
        double rentAmount = rentInfo.rentAmount;
        LocalDate rentDate = rentInfo.adjustedStartDate;

        LocalDate waterDate = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonth(), 8);
        double total = rentAmount + waterAmount + maintenanceAmount;

        int statementId = insertBillingStatement(tenantId, waterAmount, maintenanceAmount, damage, rentAmount, total, waterDate);

        if (statementId == -1){
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate billing statement");
            return;
        }

        String templatePath = "C:\\Users\\User\\OneDrive\\Documents\\templates\\billingStatement.pdf";
        String outputPath = "C:\\Users\\User\\OneDrive\\Documents\\generated\\BillingStatement_" + statementId + ".pdf";


        LocalDate maintenanceDateValue = maintenanceDate.getValue() == null ? rentDate : LocalDate.now();

        generatePDF(
                templatePath,
                outputPath,
                nameField.getText(),
                roomLabel.getText(),
                phoneLabel.getText(),
                statementId,
                LocalDate.now(),
                rentAmount,
                rentDate,
                waterAmount,
                waterDate,
                maintenanceAmount,
                maintenanceDateValue,
                damage,
                total
        );

        AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Success", "Billing statement generated: " + outputPath);

        try {
            showPreview(outputPath, statementId);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int insertBillingStatement(int tenantId, double water, double maintenance, String damage,
                                       double rent, double total, LocalDate billDate) {
        String sql = """
                INSERT INTO billingStatement(tenantId, waterAmount, maintenanceAmount, damage, rentAmount, totalAmount, waterBillDate)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DbConn.connectDB()) {
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, tenantId);
            stmt.setDouble(2, water);
            stmt.setDouble(3, maintenance);
            stmt.setString(4, damage);
            stmt.setDouble(5, rent);
            stmt.setDouble(6, total);
            stmt.setDate(7, Date.valueOf(billDate));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    private void generatePDF(String templatePath, String outputPath,
                             String name, String room, String phone,
                             int statementNumber, LocalDate statementDate,
                             double rent, LocalDate rentDate, double water,
                             LocalDate waterDate, double maintenance,
                             LocalDate maintenanceDate, String damage, double total) {

        try (PDDocument doc = PDDocument.load(new File(templatePath))) {

            PDPage page = doc.getPage(0);

            try (PDPageContentStream content = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                InputStream fontStream = getClass().getResourceAsStream("/Font/DejaVuSans.ttf");
                if (fontStream == null) throw new RuntimeException("Font file not found in /Font/DejaVuSans.ttf");

                PDType0Font font = PDType0Font.load(doc, fontStream, true);
                content.setFont(font, 12);
                content.setNonStrokingColor(0, 0, 0);

                content.beginText();
                float cm = 28.3464567f;
                float y = 29.7f;

                writeText(content, name, 2.29f * cm, (y - 5.97f) * cm);
                writeText(content, room, 2.31f * cm, (y - 6.9f)* cm);
                writeText(content, phone, 2.29f * cm, (y - 7.84f) * cm);
                writeText(content, "#" + statementNumber, 10.90f * cm, (y - 5.99f) * cm);
                writeText(content, statementDate.toString(), 10.90f * cm, (y - 7.92f) * cm);
                writeText(content, "₱ " + rent, 8.77f * cm, (y - 16.65f) * cm);
                writeText(content, rentDate.toString(), 14.48f * cm, (y - 16.65f)* cm);
                writeText(content, "₱ " + water, 8.77f * cm, (y -17.77f) * cm);
                writeText(content, waterDate.toString(), 14.48f * cm, (y - 17.77f)* cm);
                writeText(content, "₱ " + maintenance, 8.77f * cm, (y - 18.72f) * cm);
                writeText(content, maintenanceDate.toString(), 14.48f * cm, (y - 18.75f) * cm);
                writeText(content, damage, 4.60f * cm, (y - 20.19f) * cm);
                writeText(content, "₱ " + total, 2.55f * cm, (y - 25.2f) * cm);
                content.endText();
            }

            File outFile = new File(outputPath);
            outFile.getParentFile().mkdirs();
            doc.save(outFile);
            System.out.println("PDF generated and saved to: " + outFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private RentInfo getRentInfo(int unitId) {
        String sql = """
                SELECT b.rentAmount, r.startDate FROM billing b  
                JOIN roomAccount r ON b.unitId = r.unitId
                WHERE b.unitId = ?
                """;

        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setInt(1, unitId);
             ResultSet rs = stmt.executeQuery();

             if (rs.next()) {
                 double rent = rs.getDouble("rentAmount");
                 LocalDate startDate = rs.getDate("startDate").toLocalDate();

                 LocalDate now = LocalDate.now();
                 int day = startDate.getDayOfMonth();

                 int safeDay = Math.min(day, now.lengthOfMonth());
                 LocalDate adjusted = LocalDate.of(now.getYear(), now.getMonth(), safeDay);

                 return new RentInfo(rent, adjusted);
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new RentInfo(0, LocalDate.now());
    }

    private static class RentInfo {
        double rentAmount;
        LocalDate adjustedStartDate;
        RentInfo(double rentAmount, LocalDate adjustedStartDate) {
            this.rentAmount = rentAmount;
            this.adjustedStartDate = adjustedStartDate;
        }
    }

    private static void writeText(PDPageContentStream content, String text, float x, float y) throws Exception {
        content.newLineAtOffset(x, y);
        content.showText(text == null ? "" : text);
        content.newLineAtOffset(-x, -y);
    }

    private void showPreview(String pdfPath, int statementId){
        try (PDDocument doc = PDDocument.load(new File(pdfPath))) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage awtImage = renderer.renderImageWithDPI(0, 150);
            Image fxImage = SwingFXUtils.toFXImage(awtImage, null);

            ImageView previewView = new ImageView(fxImage);
            previewView.setPreserveRatio(true);
            previewView.setFitWidth(600);
            previewView.setFitHeight(800);

            Button previewBtn = new Button("Open External");
            Button sendBtn = new Button("send");
            Button cancelBtn = new Button("cancel");

            HBox buttons = new HBox(10, previewBtn, sendBtn, cancelBtn);
            VBox root = new VBox(10, previewView, buttons);
            root.setStyle("-fx-padding: 19; -fx-background-color: white;");

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Preview Billing Statement");
            dialog.setScene(new Scene(root));
            dialog.setMinWidth(640);
            dialog.setMinHeight(900);

            previewBtn.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().open(new File(pdfPath));
                } catch (Exception ex){
                    ex.printStackTrace();
                    AlertMessage.showAlert(Alert.AlertType.ERROR, "Open Failed", "Can't open PDF");
                }
            });

            sendBtn.setOnAction(e -> {
                sendBtn.setDisable(true);
                boolean ok = markStatementAsSent(statementId, pdfPath);

                if (ok) {
                    AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Sent", "Billing statement already sent!");
                } else {
                    AlertMessage.showAlert(Alert.AlertType.ERROR, "Sent Failed", "Billing wasn't sent");
                    sendBtn.setDisable(false);
                    return;
                }
                dialog.close();
            });

            cancelBtn.setOnAction(e -> dialog.close());

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean markStatementAsSent(int statementId, String pdfPath) {
        String sql = "UPDATE billingStatement SET pdfPath = ?, isSent = ?, sendDate = ? WHERE billingStatementId = ?";
        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, pdfPath);
            stmt.setBoolean(2, true);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setInt(4, statementId);

            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
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

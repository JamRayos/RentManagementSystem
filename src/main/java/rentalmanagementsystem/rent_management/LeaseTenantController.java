package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class LeaseTenantController {
    @FXML
    WebView leaseWebView;

    int tenantId = SessionLogin.getCurrentTenant().getTenantId();
    String name = SessionLogin.getCurrentTenant().getName();

    @FXML
    AnchorPane drawerPane;
    @FXML
    Button menuButton;
    private boolean drawerOpen = true;

    @FXML public void initialize() {
        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        loadLeasePreview(tenantId);
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


    private void loadLeasePreview(int tenatId){
        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(
                    """
            SELECT t.name, b.billingPeriod, b.rentAmount, r.startDate, r.endDate, 
            l.financialTerms, l.TermsRenewal, l.Occupancy, l.Violations
            FROM tenantAccount t 
            JOIN leaseAgreement l ON t.leaseAgreementId = l.leaseAgreementId
            JOIN roomAccount r ON t.unitId = r.unitId
            JOIN billing b ON r.unitId = b.unitId
            WHERE t.tenantAccountId = ?
            """);

            stmt.setInt(1, tenatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                double rent = rs.getDouble("rentAmount");
                String billing = rs.getString("billingPeriod");
                LocalDate start = rs.getDate("startDate").toLocalDate();
                LocalDate end = rs.getDate("endDate").toLocalDate();

                String dueDate = computeDueDate(start, billing);

                String html = """
                        <html>
                       
                        <head>
                        </head>
                        
                        <body>
                            <h1>Pavilion Lease Agreement</h1>
                            <p> </p>
                            <p><strong>Tenant:</strong> %s</p>
                            <p><strong>Lease Start:</strong> %s</p>
                            <p><strong>Lease End:</strong> %s</p>
                            <h3>_______________________________________________________________________________</h3>
                            <p><strong>Rent Amount:</strong> %.2f</p>
                            <p><strong>Billing Period:</strong> %s</p>
                            <p><strong>Due Date:</strong> %s</p>
                            <h3>_______________________________________________________________________________</h3>
                            \s
                            <div class="section">
                                <h2>Financial Terms</h2>
                                <p>%s</p>
                            </div>
                            <h3>_______________________________________________________________________________</h3>
                            <div class="section">
                                <h2>Terms & Renewal</h2>
                                <p>%s</p>
                            </div>
                            <h3>_______________________________________________________________________________</h3>
                            <div class="section">
                                <h2>Occupancy</h2>
                                <p>%s</p>
                            </div>
                            <h3>_______________________________________________________________________________</h3>
                            <div class="section">
                                <h2>Violations</h2>
                                <p>%s</p>
                            </div>
                            <h3>_______________________________________________________________________________</h3>
                        </body>
                        
                        </html>
                        """.formatted(name, start, end, rent, billing, dueDate,
                                    rs.getString("financialTerms"),
                                    rs.getString("TermsRenewal"),
                                    rs.getString("Occupancy"),
                                    rs.getString("Violations")
                        );
                leaseWebView.getEngine().loadContent(html);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private String computeDueDate(LocalDate start, String billingPeriod) {
        int day = start.getDayOfMonth();
        String daySuffix = getDaySuffix(day); // add "st", "nd", "rd", "th"

        switch (billingPeriod.toLowerCase()) {
            case "monthly":
                return String.format("%d%s of every month", day, daySuffix);
            case "quarterly":
                return String.format("%d%s of every quarter", day, daySuffix);
            case "semi-annual":
                return String.format("%d%s every 6 months", day, daySuffix);
            case "annual":
                return String.format("%d%s of every year", day, daySuffix);
            default:
                return String.format("%d%s of billing cycle", day, daySuffix);
        }
    }

    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) return "th";
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    @FXML private void handleDownload() {
        try {
            WebEngine engine = leaseWebView.getEngine();
            String html = (String) engine.executeScript("document.documentElement.outerHTML");

            String userHome = System.getProperty("user.home");
            String pdfPath = Paths.get(userHome, "Downloads", "lease_" + name.replace(" ", "_") + ".pdf").toString();

            try (OutputStream file = new FileOutputStream(pdfPath)) {

                com.lowagie.text.Document document = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(document, file);

                document.open();

                // Convert HTML → PDF
                com.lowagie.text.html.simpleparser.HTMLWorker htmlWorker =
                        new com.lowagie.text.html.simpleparser.HTMLWorker(document);

                htmlWorker.parse(new StringReader(html));

                document.close();
            }

            AlertMessage.showAlert(Alert.AlertType.INFORMATION,
                    "Lease Agreement Downloaded",
                    "Lease Agreement saved as:\n" + pdfPath);

        } catch (Exception e) {
            e.printStackTrace();
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate PDF");
        }
    }

    @FXML private void paymentMethodButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("optionPayMethod.fxml"); }
    @FXML private void complaintsTenantButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("complaintTenant.fxml"); }
    @FXML private void billingTenantButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("tenantBilling.fxml"); }
    @FXML private void helpNSupportButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("helpNSupportUI.fxml"); }
    @FXML private void PaymentHistoryButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("PaymentHistoryUI.fxml"); }
    @FXML private void contactNAboutButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("contactNAbout.fxml"); }
    @FXML private void dashboardButton(javafx.event.ActionEvent event) throws IOException { SceneManager.switchScene("tenantDashboard.fxml"); }


    @FXML
    private void logoutButton(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            SceneManager.switchScene("loginPage.fxml");
        } else {
            alert.close();
        }
    }

}

package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class leaseManagementController {

    @FXML
    AnchorPane drawerPane;
    @FXML Button burger;
    private boolean drawerOpen = true;

    @FXML TableView<leaseManagementDisplay> leaseManagementTable;
    @FXML TableColumn<leaseManagementDisplay, String> nameColumn;
    @FXML TableColumn <leaseManagementDisplay, Integer> idColumn;
    @FXML TableColumn <leaseManagementDisplay, String> unitNoColumn;
    @FXML TableColumn <leaseManagementDisplay, String> paymentPeriodColumn;
    @FXML TableColumn <leaseManagementDisplay, LocalDateTime> leaseStartColumn;
    @FXML TableColumn <leaseManagementDisplay, LocalDateTime> leaseEndColumn;

    @FXML private TextArea sectionContent;
    @FXML private Button editBtn, saveBtn;
    @FXML private Button btnFinancialTerms, btnTermsRenewal, btnOccupancy, btnViolations;

    private int currentLeaseId = -1;
    private String currentSection = null;
    private String financialTerms, termsRenewal, occupancy, violations;

    @FXML private Label nameLabel;
    @FXML private Label unitLabel;
    @FXML private Label startLabel;
    @FXML private Label endLabel;
    @FXML private Label payPeriodLabel;

    ObservableList<leaseManagementDisplay> data = FXCollections.observableArrayList();

    @FXML
    public void initialize(){

        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tenantAccountId"));
        unitNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        paymentPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
        leaseStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        leaseEndColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        loadData();

        leaseManagementTable.setOnMouseClicked(event -> {
            leaseManagementDisplay selected = leaseManagementTable.getSelectionModel().getSelectedItem();
            if (selected != null) loadLeaseDetails(selected.getTenantAccountId());
        });

        leaseManagementTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                showTenantDetails(newValue));

        btnFinancialTerms.setOnAction(e -> showSection("Financial Terms"));
        btnTermsRenewal.setOnAction(e -> showSection("Terms & Renewal"));
        btnOccupancy.setOnAction(e -> showSection("Occupancy"));
        btnViolations.setOnAction(e -> showSection("Violations"));

        editBtn.setOnAction(e -> toggleEditMode(true));
        saveBtn.setOnAction(e -> publishNewVersion());
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

    private void loadData() {
        data.clear();

        try (Connection conn = DbConn.connectDB()){
            String query = """
                    SELECT p.name, p.tenantAccountId, r.roomNo, b.billingPeriod, r.startDate, r.endDate FROM tenantAccount p
                    JOIN roomAccount r ON  p.unitId = r.unitId
                    JOIN billing b ON p.unitId = b.unitId
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("startDate");
                Timestamp timestamp1 = rs.getTimestamp("endDate");
                LocalDateTime startDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                LocalDateTime endDate = timestamp1 != null ? timestamp1.toLocalDateTime() : null;

                data.add(new leaseManagementDisplay(
                        rs.getString("name"),
                        rs.getInt("tenantAccountId"),
                        rs.getString("roomNo"),
                        rs.getString("billingPeriod"),
                        startDate,
                        endDate
                ));
            }
            leaseManagementTable.setItems(data);
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void loadLeaseDetails(int tenantId) {
        try (Connection conn = DbConn.connectDB()){
            String query =  """
                    SELECT l.leaseAgreementId, l.financialTerms, l.TermsRenewal, l.Occupancy, l.Violations
                    FROM leaseAgreement l
                    JOIN tenantAccount t ON t.leaseAgreementId = l.leaseAgreementId
                    WHERE t.tenantAccountId = ?
                    """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentLeaseId = rs.getInt("leaseAgreementId");
                financialTerms = rs.getString("financialTerms");
                termsRenewal = rs.getString("TermsRenewal");
                occupancy = rs.getString("Occupancy");
                violations = rs.getString("Violations");
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void showSection(String section){
        currentSection = section;
        sectionContent.setEditable(false);
        saveBtn.setDisable(true);

        switch (section){
            case "Financial Terms" -> sectionContent.setText(financialTerms);
            case "Terms & Renewal" -> sectionContent.setText(termsRenewal);
            case "Occupancy" -> sectionContent.setText(occupancy);
            case "Violations" -> sectionContent.setText(violations);
        }
    }

    private void toggleEditMode(boolean editable) {
        sectionContent.setEditable(editable);
        saveBtn.setDisable(!editable);
    }

    private void publishNewVersion() {
        String updateText = sectionContent.getText();

        switch (currentSection){
            case "Financial Terms" -> financialTerms = updateText;
            case "Terms & Renewal" -> termsRenewal = updateText;
            case "Occupancy" -> occupancy = updateText;
            case "Violations" -> violations = updateText;
        }

        try (Connection conn = DbConn.connectDB()){
            String insert = """
                    INSERT INTO leaseAgreement (financialTerms, TermsRenewal, Occupancy, Violations, dateCreated) VALUES
                    (?, ?, ?, ?, NOW())
                    """;
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, financialTerms);
            stmt.setString(2, termsRenewal);
            stmt.setString(3, occupancy);
            stmt.setString(4, violations);
            stmt.executeUpdate();

            System.out.println("New lease version published successfully:)");
            toggleEditMode(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showTenantDetails(leaseManagementDisplay tenant){
        if (tenant != null) {
            nameLabel.setText(tenant.getName());
            unitLabel.setText(tenant.getRoomNo());
            payPeriodLabel.setText(tenant.getBillingPeriod());

            if (tenant.getStartDate() != null)
                startLabel.setText(tenant.getStartDate().toLocalDate().toString());
            if (tenant.getEndDate() != null)
                endLabel.setText(tenant.getEndDate().toLocalDate().toString());
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

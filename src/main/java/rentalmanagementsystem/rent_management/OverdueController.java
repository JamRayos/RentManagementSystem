package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OverdueController {

    @FXML
    AnchorPane drawerPane;
    @FXML
    Button burger;
    private boolean drawerOpen = true;

    @FXML TableView<OverdueDisplay> overdueTableView;
    @FXML TableColumn<OverdueDisplay, String> nameColumn;
    @FXML TableColumn<OverdueDisplay, Integer> idColumn;
    @FXML TableColumn<OverdueDisplay, String> unitNoColumn;
    @FXML TableColumn<OverdueDisplay, Double> overdueBalanceColumn;
    @FXML TableColumn<OverdueDisplay, Integer> daysOverdueColumn;
    @FXML TableColumn<OverdueDisplay, Boolean> evictionColumn;
    @FXML TableColumn<OverdueDisplay, String> dueDateColumn;
    @FXML ComboBox<String> reasonDropdown;
    @FXML private TextArea reasonDetails;
    @FXML private DatePicker evictionDatePicker;

    @FXML private TextField nameField;
    @FXML private Label unitLabel;
    @FXML private TextField emailField;

    @FXML private ComboBox<String> floorFilter;
    private ObservableList<OverdueDisplay> overdueData = FXCollections.observableArrayList();
    private final ContextMenu suggestionsMenu = new ContextMenu();
    private int tenantId;
    private int unitId;

    @FXML public void initialize() {

        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tenantAccountId"));
        unitNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        overdueBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("overdueBalance"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDueDate"));
        daysOverdueColumn.setCellValueFactory(new PropertyValueFactory<>("daysOverdue"));
        evictionColumn.setCellValueFactory(new PropertyValueFactory<>("archived"));

        nameField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String query = nameField.getText().trim();
            if (!query.isEmpty()) {
                showTenantSuggestions(query);
            } else {
                suggestionsMenu.hide();
            }
        });

        reasonDropdown.getItems().addAll(
                "Failure to pay rent",
                "Breach of agreement",
                "Illegal activity"
        );

        overdueTableView.setItems(overdueData);
        loadFloors();
        loadOverdueTable();

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
                SELECT t.tenantAccountId, t.name, t.unitId, t.email, r.roomNo FROM tenantAccount t
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
                String email = rs.getString("email");

                MenuItem item = new MenuItem(name + " - Room " + roomNo);
                item.setOnAction(e -> {
                    tenantId = id;
                    unitId = unit;
                    nameField.setText(name);
                    unitLabel.setText(roomNo);
                    emailField.setText(email);
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

    private void loadFloors() {
        ObservableList<String> floors = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT floorId FROM roomAccount ORDER BY floorId ASC";

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                floors.add(rs.getString("floorId"));
            }

            floorFilter.setItems(floors);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void loadOverdueTable() {
        loadOverdueTable(null);
    }

    private void loadOverdueTable(String selectedFloor) {
        ObservableList<OverdueDisplay> data = FXCollections.observableArrayList();

        String query = """
        SELECT 
            t.name,
            t.tenantAccountId,
            r.roomNo,
            r.floorId,
            b.currentBalance,
            b.rentAmount,
            r.startDate,
            b.billingPeriod,
            t.archived
        FROM tenantAccount t
        JOIN roomAccount r ON t.unitId = r.unitId
        JOIN billing b ON b.unitId = r.unitId
        WHERE b.currentBalance > 0
        """;

        if (selectedFloor != null && !selectedFloor.isEmpty()) {
            query += " AND r.floorId = ?";
        }

        try (Connection conn = DbConn.connectDB()) {

            PreparedStatement stmt = conn.prepareStatement(query);
            if (selectedFloor != null && !selectedFloor.isEmpty()) {
                stmt.setInt(1, Integer.parseInt(selectedFloor));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                int tenantAccountId = rs.getInt("tenantAccountId");
                String roomNo = rs.getString("roomNo");
                double currentBalance = rs.getDouble("currentBalance");
                double rentAmount = rs.getDouble("rentAmount");
                LocalDateTime startDate = rs.getDate("startDate").toLocalDate().atStartOfDay();
                String billingPeriod = rs.getString("billingPeriod");
                boolean archived = rs.getBoolean("archived");

                // Compute the most recent due date
                LocalDateTime dueDate = calculateDueDate(startDate.toLocalDate(), billingPeriod);
                LocalDateTime now = LocalDateTime.now();

                // Only consider overdue if current balance is greater than 0 AND due date has passed
                int daysOverdue = 0;
                if (currentBalance > 0 && now.toLocalDate().isAfter(dueDate.toLocalDate())) {
                    daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
                }

                data.add(new OverdueDisplay(
                        name,
                        tenantAccountId,
                        roomNo,
                        currentBalance,
                        dueDate,
                        daysOverdue,
                        archived
                ));
            }

            overdueData.setAll(data);
            overdueTableView.setItems(overdueData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void sendEvictionNotice(){
        String reason = reasonDropdown.getValue();
        String details = reasonDetails.getText();
        LocalDate evictionDate = evictionDatePicker.getValue();

        if (reason == null || details.isBlank() || evictionDate == null){
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Missing Info", "Please fill out all fields");
            return;
        }

        try (Connection conn = DbConn.connectDB()){
            String tenantEmail = getTenantEmail(tenantId, conn);
            if (tenantEmail == null){
                AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Tenant email not found");
                return;
            }

            String subject = "Eviction Notice";
            String message = """
                    Dear Tenant,
                    
                    This is to inform you that an eviction notice has been issued due to the following reason:
                    
                    Reason: %s
                    Details: %s
                    
                    Eviction Date: %s
                    
                    Please vacate the premises before the mentioned date. Failure to do so may result in legal action
                    
                    Regards,
                    Rent Management Office 
                    """.formatted(reason, details, evictionDate);

            EmailSender.sendEmail(tenantEmail, subject, message);

            String insertNotice = """
                    INSERT INTO evictionNotice (tenantId, noticeDate, details, evictionDate, status, reason)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertNotice)) {
                ps.setInt(1, tenantId);
                ps.setDate(2, Date.valueOf(LocalDate.now()));
                ps.setString(3, details);
                ps.setDate(4, Date.valueOf(evictionDate));
                ps.setString(5, "pending");
                ps.setString(6,  reason);
                ps.executeUpdate();
            }

            AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Eviction Notice Sent",
                    "Eviction notice successfully sent to tenant email");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getTenantEmail(int tenantAccountId, Connection conn) throws SQLException {
        String query = "SELECT email FROM tenantAccount WHERE tenantAccountId = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, tenantAccountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("email");
            }
        }
        return null;
    }

    private LocalDateTime calculateDueDate(LocalDate startDate, String billingPeriod) {
        int monthsToAdd;
        switch (billingPeriod.toLowerCase()) {
            case "monthly" -> monthsToAdd = 1;
            case "quarterly" -> monthsToAdd = 3;
            case "semi-annual" -> monthsToAdd = 6;
            case "annual" -> monthsToAdd = 12;
            default -> monthsToAdd = 1;
        }

        LocalDate dueDate = startDate;
        LocalDate today = LocalDate.now();

        // Keep adding periods until dueDate is the next due
        while (!dueDate.isAfter(today)) {
            dueDate = dueDate.plusMonths(monthsToAdd);
        }

        // Return the *most recent past due date*
        return dueDate.minusMonths(monthsToAdd).atStartOfDay();
    }

    @FXML
    private void filterByFloor() {
        String selectedFloor = floorFilter.getValue();
        loadOverdueTable(selectedFloor);
    }

    @FXML
    private void clearFloorFilter() {
        floorFilter.getSelectionModel().clearSelection();
        loadOverdueTable();
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

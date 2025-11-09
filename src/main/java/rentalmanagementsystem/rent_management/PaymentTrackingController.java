package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import javafx.scene.chart.XYChart;

import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentTrackingController {

    @FXML AnchorPane drawerPane;
    @FXML Button burger;
    private boolean drawerOpen = true;

    @FXML TextField tenantNameField;
    @FXML TextField amountField;
    @FXML AnchorPane addPaymentPane;
    @FXML Label roomNoLabel;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private CategoryAxis monthAxis;
    @FXML NumberAxis revenueAxis;

    @FXML TableView <PaymentDisplay> paymentHistoryTable;
    @FXML TableColumn<PaymentDisplay, String> nameColumn;
    @FXML TableColumn<PaymentDisplay, Integer> idColumn;
    @FXML TableColumn<PaymentDisplay, String> unitColumn;
    @FXML TableColumn<PaymentDisplay, Double> amountPaidColumn;
    @FXML TableColumn<PaymentDisplay, LocalDateTime> paymentDateColumn;
    @FXML TableColumn<PaymentDisplay, String> modeOfPaymentColumn;

    ObservableList<PaymentDisplay> data = FXCollections.observableArrayList();

    private final ContextMenu suggestionsMenu = new ContextMenu();
    private int tenantId;
    private int unitId;

    @FXML private Label rentCollectedLabel;
    @FXML private Label totalOverdueLabel;
    @FXML private Label totalPaidLabel;

    @FXML public void initialize() {

        //navdrawer initialization
        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tenantAccountId"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        amountPaidColumn.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        modeOfPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("modeOfPayment"));

        loadDataFromDatabase();
        loadMonthlyRevenueData();
        loadMonthlySummary();

        tenantNameField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() >= 2) {
                showTenantSuggestions(newText);
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
                SELECT t.tenantAccountId, t.name, t.unitId, r.roomNo FROM tenantAccount t
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

                MenuItem item = new MenuItem(name + " - Room " + roomNo);
                item.setOnAction(e -> {
                    tenantId = id;
                    unitId = unit;
                    tenantNameField.setText(name);
                    roomNoLabel.setText(roomNo);
                    suggestionsMenu.hide();
                });
                suggestionsMenu.getItems().add(item);

            }

            if (hasResults) {
                suggestionsMenu.show(tenantNameField, Side.BOTTOM, 0, 0);
            } else {
                suggestionsMenu.hide();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void paymentTrackingDAO(PaymentHistory history) {
        String sql = """
                INSERT INTO paymentTracking (tenantId, modeOfPayment, amountPaid, paymentDate, paymentStatus)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, tenantId);
            stmt.setString(2, history.getModeOfPayment());
            stmt.setDouble(3, history.getAmountPaid());
            stmt.setTimestamp(4, Timestamp.valueOf(history.getPaymentDate()));
            stmt.setString(5, history.getPaymentStatus());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) history.setPaymentTrackingId(keys.getInt(1));

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private String calculatePaymentStatus(int unitId) {
        String sql = """
                SELECT r.startDate, b.billingPeriod FROM roomAccount r
                JOIN billing b ON r.unitId = b.unitId
                WHERE r.unitId = ?
                """;
        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, unitId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate startDate = rs.getDate("startDate").toLocalDate();
                String billingPeriod = rs.getString("billingPeriod").toLowerCase();
                LocalDate today = LocalDate.now();

                LocalDate dueDate = calculateDueDate(startDate, billingPeriod, today);

                if (today.isEqual(dueDate) || today.isBefore(dueDate)) {
                    return "Paid";
                } else {
                    return "late";
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return "Unknown";
    }

    private LocalDate calculateDueDate(LocalDate startDate, String billingPeriod, LocalDate today){
        int monthsToAdd;
        switch (billingPeriod) {
            case "monthly" -> monthsToAdd = 1;
            case "quarterly" -> monthsToAdd = 3;
            case "semi-annual" -> monthsToAdd = 6;
            case "annual" -> monthsToAdd = 12;
            default -> monthsToAdd = 1;
        }

        LocalDate dueDate = startDate;
        while (dueDate.isBefore(today)){
            dueDate = dueDate.plusMonths(monthsToAdd);
        }
        return dueDate;
    }

    @FXML
    private void handleAddPayment() {
        String name = tenantNameField.getText();
        String amountText = amountField.getText();

        if (name.isEmpty() || amountText.isEmpty()){
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Please fill out all fields.");
            return;
        }

        double amount;
        try{
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount");
            return;
        }

        double currentBalance = getCurrentBalance(unitId);

        if (amount > currentBalance) {
            AlertMessage.showAlert(Alert.AlertType.WARNING, "Warning", "Payment exceeds current balance. Tenant only owes " + currentBalance);
            return;
        }

        String paymentstatus = calculatePaymentStatus(unitId);

        PaymentHistory history = new PaymentHistory(
                0,
                tenantId,
                "cash",
                amount,
                java.time.LocalDateTime.now(),
                paymentstatus
        );

        paymentTrackingDAO(history);
        updateBillingBalance(unitId, amount);

        AlertMessage.showAlert(Alert.AlertType.INFORMATION, "Success", "Payment recorded successfully");
        addPaymentPane.setVisible(false);
    }

    private double getCurrentBalance(int unitId) {
        String sql = "SELECT currentBalance FROM billing WHERE unitId = ?";
        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, unitId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                return rs.getDouble("currentBalance");
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0.0;
    }

    private void loadDataFromDatabase() {
        data.clear();

        try(Connection conn = DbConn.connectDB()) {
            String query = """
                    SELECT p.name, p.tenantAccountId, r.roomNo, pt.amountPaid, pt.paymentDate, pt.modeOfPayment
                    FROM tenantAccount p
                    JOIN roomAccount r ON p.unitId = r.unitId
                    JOIN paymentTracking pt ON p.tenantAccountId = pt.tenantId
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("paymentDate");
                LocalDateTime paymentDate = timestamp != null ? timestamp.toLocalDateTime() : null;

                data.add(new PaymentDisplay(
                        rs.getString ("name"),
                        rs.getInt("tenantAccountId"),
                        rs.getString("roomNo"),
                        rs.getDouble("amountPaid"),
                        paymentDate,
                        rs.getString("modeOfPayment")
                ));
            }
            paymentHistoryTable.setItems(data);
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMonthlyRevenueData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Revenue");

        Map<String, Double> revenueByMonth = new LinkedHashMap<>();

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        int currentYear = LocalDate.now().getYear();

        for(int month = 1; month <= 12; month++){
            String key = String.format("%d-%02d", currentYear, month);
            revenueByMonth.put(key, 0.0);
        }

        String sql = """
                SELECT DATE_FORMAT(paymentDate, '%Y-%m') AS month, SUM(amountPaid) AS total
                FROM paymentTracking
                WHERE YEAR(paymentDate) = ?
                GROUP BY DATE_FORMAT(paymentDate, '%Y-%m')
                ORDER BY month;
                """;

        try(Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, currentYear);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                String month = rs.getString("month");
                double total = rs.getDouble("total");
                revenueByMonth.put(month, total);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        java.time.format.TextStyle textStyle = java.time.format.TextStyle.SHORT;
        java.util.Locale locale = java.util.Locale.ENGLISH;

        for (int month = 1; month <= 12; month++) {
            String key = String.format("%d-%02d", currentYear, month);
            String monthName = java.time.Month.of(month).getDisplayName(textStyle, locale);
            series.getData().add(new XYChart.Data<>(monthName, revenueByMonth.get(key)));
        }

        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    private void updateBillingBalance(int unitId, double paymentAmount) {
        String sql = "UPDATE billing SET currentBalance = currentBalance - ? WHERE unitId = ?";

        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, paymentAmount);
            stmt.setInt(2, unitId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMonthlySummary() {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        double rentCollected = 0.0;
        double totalOverdue = 0.0;
        int paidTenants = 0;

        String sql = """
                SELECT b.rentAmount, b.currentBalance, pt.amountPaid, pt.paymentDate
                FROM billing b 
                LEFT JOIN tenantAccount t ON t.unitId = b.unitId
                LEFT JOIN paymentTracking pt ON t.tenantAccountId = pt.tenantId
                WHERE YEAR(pt.paymentDate) = ? AND MONTH(pt.paymentDate) = ?
                """;

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, currentYear);
            stmt.setInt(2, currentMonth);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double rentAmount = rs.getDouble("rentAmount");
                double currentBalance = rs.getDouble("currentBalance");
                double paid = rs.getDouble("amountPaid");

                rentCollected += paid;

                if (paid >= rentAmount || currentBalance <= 0) {
                    paidTenants++;
                }

                if (currentBalance > rentAmount){
                    totalOverdue += currentBalance;
                }
            }

            rentCollectedLabel.setText(String.format("₱%.2f", rentCollected));
            totalPaidLabel.setText(String.valueOf(paidTenants));
            totalOverdueLabel.setText(String.format("₱%.2f", totalOverdue));

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    private void showAddPayment() {
        addPaymentPane.setVisible(true);
    }

    @FXML
    private void unitOverviewButton(ActionEvent event) throws IOException {
        SceneManager.switchScene("unitsOverview.fxml");
    }

    @FXML
    private void logOutButton(ActionEvent event) throws IOException {
        SceneManager.switchScene("login.fxml");
    }

    @FXML
    private void leaseButton(ActionEvent event) throws IOException {
        SceneManager.switchScene("leaseManagement.fxml");
    }

    @FXML
    private void linkAccount(ActionEvent event) throws IOException {
        SceneManager.switchScene("roomAccount.fxml");
    }

    @FXML
    private void overdueButton(ActionEvent event) throws IOException {
        SceneManager.switchScene("overdueTenants.fxml");
    }
}

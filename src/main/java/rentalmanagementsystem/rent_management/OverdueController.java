package rentalmanagementsystem.rent_management;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OverdueController {

    @FXML TableView<OverdueDisplay> overdueTableView;
    @FXML TableColumn<OverdueDisplay, String> nameColumn;
    @FXML TableColumn<OverdueDisplay, Integer> idColumn;
    @FXML TableColumn<OverdueDisplay, String> unitNoColumn;
    @FXML TableColumn<OverdueDisplay, Double> overdueBalanceColumn;
    @FXML TableColumn<OverdueDisplay, LocalDateTime> dueDateColumn;
    @FXML TableColumn<OverdueDisplay, Integer> daysOverdueColumn;
    @FXML TableColumn<OverdueDisplay, Boolean> evictionColumn;

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tenantAccountId"));
        unitNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        overdueBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("overdueBalance"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        daysOverdueColumn.setCellValueFactory(new PropertyValueFactory<>("daysOverdue"));
        evictionColumn.setCellValueFactory(new PropertyValueFactory<>("archived"));

        loadOverdueTable();

    }
    private void loadOverdueTable() {
        ObservableList<OverdueDisplay> data = FXCollections.observableArrayList();

        String query = """
        SELECT 
            t.name,
            t.tenantAccountId,
            r.roomNo,
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

        try (Connection conn = DbConn.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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
                        currentBalance, // overdueBalance shown from currentBalance
                        dueDate,
                        daysOverdue,
                        archived
                ));
            }

            overdueTableView.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
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


}

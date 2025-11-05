package rentalmanagementsystem.rent_management;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
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
                SELECT\s
                    t.name,
                    t.tenantAccountId,
                    r.roomNo,
                    b.rentAmount,
                    r.startDate,
                    t.archived,
                   \s
                    -- Months the tenant has stayed
                    TIMESTAMPDIFF(MONTH, r.startDate, CURDATE()) AS monthsElapsed,
                   \s
                    -- Total balance owed (monthly rent * months elapsed) - payments
                    (TIMESTAMPDIFF(MONTH, r.startDate, CURDATE()) * b.rentAmount) - COALESCE(p.totalPaid, 0) AS overdueBalance,
                   \s
                    -- Days overdue: number of days since their most recent due date (same day as startDate each month)
                    CASE\s
                        WHEN DAY(CURDATE()) > DAY(r.startDate)\s
                            THEN DATEDIFF(CURDATE(), DATE(CONCAT(YEAR(CURDATE()), '-', MONTH(CURDATE()), '-', DAY(r.startDate))))
                        ELSE 0
                    END AS daysOverdue
               \s
                FROM tenantAccount t
                JOIN roomAccount r ON t.unitId = r.unitId
                JOIN billing b ON b.unitId = r.unitId
                LEFT JOIN (
                    SELECT tenantId, SUM(amountPaid) AS totalPaid
                    FROM paymentTracking
                    GROUP BY tenantId
                ) p ON t.tenantAccountId = p.tenantId\s
               \s""";

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                data.add(new OverdueDisplay(
                        rs.getString("name"),
                        rs.getInt("tenantAccountId"),
                        rs.getString("roomNo"),
                        rs.getDouble("overdueBalance"),
                        rs.getDate("startDate").toLocalDate().atStartOfDay(),
                        rs.getInt("daysOverdue"),
                        rs.getBoolean("archived")
                ));
            }
            overdueTableView.setItems(data);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}

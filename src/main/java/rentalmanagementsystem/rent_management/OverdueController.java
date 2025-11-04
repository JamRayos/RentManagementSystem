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
        evictionColumn.setCellValueFactory(new PropertyValueFactory<>("evictionNotice"));

        loadOverdueTable();

    }
    private void loadOverdueTable() {
        ObservableList<OverdueDisplay> data = FXCollections.observableArrayList();
        String query = """
                SELECT
                        SELECT
                                t.name AS name,
                                t.tenantAccountId,
                                r.roomNo,
                                (b.rentAmount - COALESCE(SUM(p.amountPaid), 0)) AS overdueBalance,
                                MAX(r.startDate) AS dueDate,
                                GREATEST(DATEDIFF(CURDATE(), MAX(r.startDate)), 0) AS daysOverdue,
                                CASE
                                    WHEN DATEDIFF(CURDATE(), MAX(b.dueDate)) > 30 THEN TRUE
                                    ELSE FALSE
                                END AS evictionNotice
                            FROM tenantAccount t
                            JOIN roomAccount r ON t.unitId = r.unitId
                            JOIN billing b ON b.unitId = r.unitId
                            LEFT JOIN paymentTracking p ON t.tenantAccountId = p.tenantId
                            GROUP BY t.tenantAccountId, t.name, r.roomNo;
                
                """;

        try (Connection conn = DbConn.connectDB()){
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                Timestamp timestamp = rs.getTimestamp("dueDate");
                LocalDateTime dueDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                data.add(new OverdueDisplay(
                   rs.getString("name"),
                   rs.getInt("tenantAccountId"),
                   rs.getString("roomNo"),
                   rs.getDouble("overdueBalance"),
                   dueDate,
                   rs.getInt("daysOverdue"),
                   rs.getBoolean("evictionNotice")
                ));
            }
            overdueTableView.setItems(data);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}

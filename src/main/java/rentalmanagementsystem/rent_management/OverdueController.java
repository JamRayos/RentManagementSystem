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
                SELECT
                        t.name,
                        t.tenantAccountId,
                        r.roomNo,
                        b.rentAmount,
                        r.startDate,
                        b.billingPeriod,
                        t.archived,

                        TIMESTAMPDIFF(MONTH, r.startDate, CURDATE()) AS monthsElapsed,
      
                        CASE
                            WHEN b.billingPeriod = 'Monthly' THEN 1
                            WHEN b.billingPeriod = 'Quarterly' THEN 3
                            WHEN b.billingPeriod = 'Semi-Annual' THEN 6
                            WHEN b.billingPeriod = 'Annual' THEN 12
                            ELSE 1
                        END AS periodMonths,

                        FLOOR(
                            TIMESTAMPDIFF(MONTH, r.startDate, CURDATE())
                            /
                            CASE
                                WHEN b.billingPeriod = 'Monthly' THEN 1
                                WHEN b.billingPeriod = 'Quarterly' THEN 3
                                WHEN b.billingPeriod = 'Semi-Annual' THEN 6
                                WHEN b.billingPeriod = 'Annual' THEN 12
                                ELSE 1
                            END
                        ) AS periodsElapsed,
 
                        COALESCE(p.totalPaid, 0) AS totalPaid,
                  
                        FLOOR(COALESCE(p.totalPaid, 0) / NULLIF(b.rentAmount, 0)) AS paidPeriods,
                
                        ( FLOOR(
                            TIMESTAMPDIFF(MONTH, r.startDate, CURDATE())
                            /
                            CASE
                                WHEN b.billingPeriod = 'Monthly' THEN 1
                                WHEN b.billingPeriod = 'Quarterly' THEN 3
                                WHEN b.billingPeriod = 'Semi-Annual' THEN 6
                                WHEN b.billingPeriod = 'Annual' THEN 12
                                ELSE 1
                            END
                          ) * b.rentAmount
                        ) - COALESCE(p.totalPaid, 0) AS overdueBalance,
                
                        DATE_ADD(
                            r.startDate,
                            INTERVAL FLOOR(COALESCE(p.totalPaid, 0) / NULLIF(b.rentAmount, 0))
                            *
                            CASE
                                WHEN b.billingPeriod = 'Monthly' THEN 1
                                WHEN b.billingPeriod = 'Quarterly' THEN 3
                                WHEN b.billingPeriod = 'Semi-Annual' THEN 6
                                WHEN b.billingPeriod = 'Annual' THEN 12
                                ELSE 1
                            END MONTH
                        ) AS firstMissedDueDate,
                
                        CASE
                            WHEN (\s
                                FLOOR(
                                    TIMESTAMPDIFF(MONTH, r.startDate, CURDATE())
                                    /
                                    CASE
                                        WHEN b.billingPeriod = 'Monthly' THEN 1
                                        WHEN b.billingPeriod = 'Quarterly' THEN 3
                                        WHEN b.billingPeriod = 'Semi-Annual' THEN 6
                                        WHEN b.billingPeriod = 'Annual' THEN 12
                                        ELSE 1
                                    END
                                )
                                - FLOOR(COALESCE(p.totalPaid, 0) / NULLIF(b.rentAmount, 0))
                            ) > 0
                            THEN DATEDIFF(
                                    CURDATE(),
                                    DATE_ADD(
                                        r.startDate,
                                        INTERVAL FLOOR(COALESCE(p.totalPaid, 0) / NULLIF(b.rentAmount, 0))
                                        *
                                        CASE
                                            WHEN b.billingPeriod = 'Monthly' THEN 1
                                            WHEN b.billingPeriod = 'Quarterly' THEN 3
                                            WHEN b.billingPeriod = 'Semi-Annual' THEN 6
                                            WHEN b.billingPeriod = 'Annual' THEN 12
                                            ELSE 1
                                        END MONTH
                                    )
                                 )
                            ELSE 0
                        END AS daysOverdue
                
                    FROM tenantAccount t
                    JOIN roomAccount r ON t.unitId = r.unitId
                    JOIN billing b ON b.unitId = r.unitId
                    LEFT JOIN (
                        SELECT tenantId, SUM(amountPaid) AS totalPaid
                        FROM paymentTracking
                        GROUP BY tenantId
                    ) p ON t.tenantAccountId = p.tenantId
                    HAVING overdueBalance > 0
                """;

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

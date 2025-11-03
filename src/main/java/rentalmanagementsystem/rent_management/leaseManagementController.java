package rentalmanagementsystem.rent_management;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class leaseManagementController {
    @FXML
    TableView<leaseManagementDisplay> leaseManagementTable;
    @FXML
    TableColumn<leaseManagementDisplay, String> nameColumn;
    @FXML TableColumn <leaseManagementDisplay, Integer> idColumn;
    @FXML TableColumn <leaseManagementDisplay, String> unitNoColumn;
    @FXML TableColumn <leaseManagementDisplay, String> paymentPeriodColumn;
    @FXML TableColumn <leaseManagementDisplay, LocalDateTime> leaseStartColumn;
    @FXML TableColumn <leaseManagementDisplay, LocalDateTime> leaseEndColumn;

    ObservableList<leaseManagementDisplay> data = FXCollections.observableArrayList();

    @FXML
    public void initialize(){

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tenantAccountId"));
        unitNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        paymentPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
        leaseStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        leaseEndColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        loadData();
    }

    private void loadData() {
        data.clear();

        try (Connection conn = DbConn.connectDB()){
            String query = """
                    SELECT p.name, p.tenantAccountId, r.roomNo, b.billingPeriod, r.startDate, r.endDate FROM tenantAccount p
                    JOIN roomAccount r ON  p.unitId = r.unitId
                    JOIN billing b ON p.unitId = b.unitId
                    JOIN paymentTracking pt ON p.tenantAccountId = pt.tenantId;
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
}

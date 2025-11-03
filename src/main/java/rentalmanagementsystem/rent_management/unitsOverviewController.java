package rentalmanagementsystem.rent_management;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class unitsOverviewController {

    @FXML private AnchorPane drawerPane;
    @FXML private Button menuButton;
    private boolean drawerOpen = true;

    @FXML TableView<unitsOverviewDisplay> overviewTable;
    @FXML TableColumn <unitsOverviewDisplay, String> nameColumn;
    @FXML TableColumn <unitsOverviewDisplay, Integer> idColumn;
    @FXML TableColumn <unitsOverviewDisplay, String> unitNoColumn;
    @FXML TableColumn <unitsOverviewDisplay, String> statusColumn;
    @FXML TableColumn <unitsOverviewDisplay, Integer> capacityColumn;
    @FXML TableColumn <unitsOverviewDisplay, Double> areaColumn;

    ObservableList<unitsOverviewDisplay> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        //navdrawer initialization
        drawerPane.setTranslateX(-300);
        drawerOpen = false;

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tenantAccountId"));
        unitNoColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("unitStatus"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("areaSize"));

        loadData();
    }

    @FXML //for the side drawer
    private void toggleDrawer() {

        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.millis(300));
        slide.setNode(drawerPane);

        if (drawerOpen) {
            slide.setToX(-300);
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
                    SELECT p.name, p.tenantAccountId, r.roomNo, r.unitStatus, r.capacity, r.areaSize FROM tenantAccount p
                    JOIN roomAccount r ON p.unitId = r.unitId
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                data.add(new unitsOverviewDisplay(
                        rs.getString("name"),
                        rs.getInt("tenantAccountId"),
                        rs.getString("roomNo"),
                        rs.getString("unitStatus"),
                        rs.getInt("capacity"),
                        rs.getDouble("areaSize")
                ));
            }
            overviewTable.setItems(data);
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}

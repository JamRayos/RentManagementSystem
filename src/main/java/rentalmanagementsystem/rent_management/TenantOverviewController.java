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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TenantOverviewController {
    @FXML AnchorPane drawerPane;
    @FXML Button burger;
    private boolean drawerOpen = true;

    @FXML
    private ChoiceBox <String> choiceFloor;


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

    @FXML
    public void initialize() {

        //navdrawer initialization
        drawerPane.setTranslateX(-350);
        drawerOpen = false;

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        rentColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        noOfTenantsColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        choiceFloor.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) filterByFloor(newVal);
        });

        loadDataFromDatabase();
        loadFloorChoices();
    }



    private void loadDataFromDatabase () {
        data.clear();

        try(Connection conn = DbConn.connectDB()) {
            String query = """
                SELECT ta.name, r.unitId, r.roomNo, ta.contactNumber, r.price, b.paymentStatus, r.capacity
                FROM roomAccount r
                JOIN tenantAccount ta ON r.unitId = ta.unitId
                JOIN billing b ON r.unitId = b.unitId
                """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean add = data.add(new OverviewOfAllTenants(
                        rs.getString("name"),
                        rs.getInt("unitId"),
                        rs.getString("roomNo"),
                        rs.getString("contactNumber"),
                        rs.getDouble("price"),
                        rs.getString("paymentStatus"),
                        rs.getInt("capacity")
                ));
            }
            overviewOfTenantTable.setItems(data);
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFloorChoices() {
        choiceFloor.setItems(FXCollections.observableArrayList(
                "All Floors",
                "2nd Floor (B)",
                "3rd Floor (C)",
                "4th Floor (D)",
                "5th Floor (E)",
                "6th Floor (F)"
        ));
        choiceFloor.setValue("All Floors");
    }

    private void filterByFloor(String selected) {
        if (selected == null || selected.equals("All Floors")) {
            loadDataFromDatabase(); // load all tenants
            return;
        }

        String floorLetter = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
        loadDataFilteredByFloor(floorLetter);
    }

    private void loadDataFilteredByFloor(String floorLetter) {
        data.clear();
        try (Connection conn = DbConn.connectDB()) {
            String query = """
                SELECT ta.name, r.unitId, r.roomNo, ta.contactNumber, 
                       r.price, b.paymentStatus, r.capacity
                FROM roomAccount r
                JOIN tenantAccount ta ON r.unitId = ta.unitId
                JOIN billing b ON r.unitId = b.unitId
                WHERE r.roomNo LIKE ?
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + floorLetter);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.add(new OverviewOfAllTenants(
                        rs.getString("name"),
                        rs.getInt("unitId"),
                        rs.getString("roomNo"),
                        rs.getString("contactNumber"),
                        rs.getDouble("price"),
                        rs.getString("paymentStatus"),
                        rs.getInt("capacity")
                ));
            }

            overviewOfTenantTable.setItems(data);
            overviewOfTenantTable.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML TableView <OverviewOfAllTenants> overviewOfTenantTable;
    @FXML TableColumn <OverviewOfAllTenants, String> nameColumn;
    @FXML TableColumn <OverviewOfAllTenants, Integer> idColumn;
    @FXML TableColumn <OverviewOfAllTenants, String> unitColumn;
    @FXML TableColumn <OverviewOfAllTenants, String> contactColumn;
    @FXML TableColumn <OverviewOfAllTenants, Double> rentColumn;
    @FXML TableColumn <OverviewOfAllTenants, String> statusColumn;
    @FXML TableColumn <OverviewOfAllTenants, Integer> noOfTenantsColumn;

    ObservableList<OverviewOfAllTenants> data = FXCollections.observableArrayList();



    @FXML private void dashboard (ActionEvent event) throws IOException {SceneManager.switchScene("dashboardAdmin.fxml");}
    @FXML private void complaints (ActionEvent event) throws IOException {SceneManager.switchScene("adminComplaint.fxml");}
    @FXML private void tenantOverview (ActionEvent event) throws IOException {SceneManager.switchScene("overviewOfTenants.fxml");}
    @FXML private void billing (ActionEvent event) throws IOException {SceneManager.switchScene("billingStatement.fxml");}
    @FXML private void linkAccount (ActionEvent event) throws IOException {SceneManager.switchScene("roomAccount.fxml");}
    @FXML private void paymentTracking (ActionEvent event) throws IOException {SceneManager.switchScene("paymentTracking.fxml");}
    @FXML private void overdue (ActionEvent event) throws IOException {SceneManager.switchScene("overdueTenants.fxml");}
    @FXML private void lease (ActionEvent event) throws IOException {SceneManager.switchScene("leaseManagement.fxml");}
}

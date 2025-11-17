package rentalmanagementsystem.rent_management;

import eu.hansolo.toolbox.time.Times;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class HelloController {

    @FXML AnchorPane drawerPane;
    @FXML Button burger;
    private boolean drawerOpen = true;

    @FXML TableView <Room> roomTableView;
    @FXML TableColumn<Room, Integer> unitId;
    @FXML TableColumn<Room, String> roomNo;
    @FXML TableColumn<Room, LocalDateTime> startDate;
    @FXML TableColumn<Room, LocalDateTime> endDate;
    @FXML TableColumn<Room, String> unitStatus;
    @FXML TableColumn<Room, Double> price;
    @FXML TableColumn<Room, Double> areaSize;
    @FXML TableColumn<Room, Integer> capacity;
    @FXML TableColumn<Room, String> otp;

    @FXML TextField unitIdField;
    @FXML TextField occupants;
    @FXML DatePicker startDatePicker;
    @FXML DatePicker endDatePicker;
    @FXML TextField emailField;
    @FXML ComboBox<String> billingDropDown;
    @FXML Label rentAmountLabel;


    ObservableList<Room> data = FXCollections.observableArrayList();


    @FXML public void initialize() {

        //navdrawer initialization
        drawerPane.setTranslateX(-250);
        drawerOpen = false;

        unitId.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        roomNo.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        startDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        unitStatus.setCellValueFactory(new PropertyValueFactory<>("unitStatus"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        areaSize.setCellValueFactory(new PropertyValueFactory<>("areaSize"));
        capacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        otp.setCellValueFactory(new PropertyValueFactory<>("otp"));
        billingDropDown.setItems(FXCollections.observableArrayList(
                "Monthly", "Quarterly", "Semi-annual", "Annual"
        ));

        loadDataFromDatabase();
    }

    @FXML //for the side drawer
    private void toggleDrawer() {

        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.millis(300));
        slide.setNode(drawerPane);

        if (drawerOpen) {
            slide.setToX(-200);
            drawerOpen = false;
        } else {
            slide.setToX(0);
            drawerOpen = true;
        }
        slide.play();
    }

    private void loadDataFromDatabase() {
        data.clear();

        try {
            Connection conn = DbConn.connectDB();
            String query = """
                    SELECT * FROM roomaccount
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                Timestamp timestamp = rs.getTimestamp("startDate");
                Timestamp timeStamp = rs.getTimestamp("endDate");
                LocalDateTime startDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                LocalDateTime endDate = timeStamp != null ? timeStamp.toLocalDateTime() : null;

                data.add(new Room(
                        rs.getInt("unitId"),
                        rs.getString("roomNo"),
                        startDate,
                        endDate,
                        rs.getString("unitStatus"),
                        rs.getDouble("price"),
                        rs.getDouble("areaSize"),
                        rs.getInt("capacity"),
                        rs.getString("otp")
                        ));
            }
            roomTableView.setItems(data);
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void loadDataFromDatabaseVacant() {
        data.clear();

        try {
            Connection conn = DbConn.connectDB();
            String query = """
                    SELECT * FROM roomaccount WHERE unitStatus = 'vacant'
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                Timestamp timestamp = rs.getTimestamp("startDate");
                Timestamp timeStamp = rs.getTimestamp("endDate");
                LocalDateTime startDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                LocalDateTime endDate = timeStamp != null ? timeStamp.toLocalDateTime() : null;

                data.add(new Room(
                        rs.getInt("unitId"),
                        rs.getString("roomNo"),
                        startDate,
                        endDate,
                        rs.getString("unitStatus"),
                        rs.getDouble("price"),
                        rs.getDouble("areaSize"),
                        rs.getInt("capacity"),
                        rs.getString("otp")
                ));
            }
            roomTableView.setItems(data);
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void loadDataFromDatabaseOccupied() {
        data.clear();

        try {
            Connection conn = DbConn.connectDB();
            String query = """
                    SELECT * FROM roomaccount WHERE unitStatus = 'occupied'
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                Timestamp timestamp = rs.getTimestamp("startDate");
                Timestamp timeStamp = rs.getTimestamp("endDate");
                LocalDateTime startDate = timestamp != null ? timestamp.toLocalDateTime() : null;
                LocalDateTime endDate = timeStamp != null ? timeStamp.toLocalDateTime() : null;

                data.add(new Room(
                        rs.getInt("unitId"),
                        rs.getString("roomNo"),
                        startDate,
                        endDate,
                        rs.getString("unitStatus"),
                        rs.getDouble("price"),
                        rs.getDouble("areaSize"),
                        rs.getInt("capacity"),
                        rs.getString("otp")
                ));
            }
            roomTableView.setItems(data);
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML private void sendLink(){
        try {
            String otpCode = EmailSender.generateOTP();
            int unitId = Integer.parseInt(unitIdField.getText());
            LocalDateTime start = startDatePicker.getValue().atStartOfDay();
            LocalDateTime end = endDatePicker.getValue().atStartOfDay();
            String billingPeriod = billingDropDown.getValue();
            int occupantCount = Integer.parseInt(occupants.getText());

            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null || billingDropDown.getValue() == null) {
                AlertMessage.showAlert(AlertType.WARNING, "Incomplete Data", "Please fill out all fields before sending.");
                return;
            }

            String tenantEmail = emailField.getText();
            Room selectedRoom = findRoomById(unitId);
            if (selectedRoom == null) {
                AlertMessage.showAlert(AlertType.ERROR, "Not Found", "No room found with ID: " + unitId);
                return;
            }

            double baseMonthlyRent = selectedRoom.getPrice();
            double rentAmount;

            switch (billingPeriod.toLowerCase()) {
                case "monthly" -> rentAmount = baseMonthlyRent;
                case "quarterly" -> rentAmount = baseMonthlyRent * 3;
                case "semi-annual" -> rentAmount = baseMonthlyRent * 6;
                case "annual" -> rentAmount = baseMonthlyRent * 12;
                default -> rentAmount = baseMonthlyRent;
            }

            double advanceBalance = 10000;
            double currentBalance = 0.0;

            Room room = new Room(unitId, selectedRoom.getRoomNo(), start, end, "occupied",
                    selectedRoom.getPrice(), selectedRoom.getAreaSize(), selectedRoom.getCapacity(), otpCode);
            Billing billing = new Billing(0, unitId, rentAmount, billingDropDown.getValue(), "Paid", currentBalance, advanceBalance);

            RoomAccountDAO dao = new RoomAccountDAO();
            int billingId = dao.linkTenant(room, billing);

            if (billingId > 0) {
                String insertOccupants = """
                INSERT INTO numberOfTenants (unitId, occupants)
                VALUES (?, ?)
                """;

                try (Connection conn = DbConn.connectDB();
                     PreparedStatement stmt = conn.prepareStatement(insertOccupants)) {

                    stmt.setInt(1, unitId);
                    stmt.setInt(2, occupantCount);
                    stmt.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertMessage.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save occupants: " + e.getMessage());
                }
                AlertMessage.showAlert(AlertType.INFORMATION, "Success", "Tenant linked with Billing ID: " + billingId);
                new Thread(() -> {
                    EmailSender.sendOTP(tenantEmail, selectedRoom.getRoomNo(), otpCode);
                }).start();

                loadDataFromDatabase();
            } else {
                AlertMessage.showAlert(AlertType.ERROR, "Error", "Failed to link tenant to unit");
            }
        } catch (Exception e){
            e.printStackTrace();
            AlertMessage.showAlert(AlertType.ERROR, "Error", "Something went wrong: " + e.getMessage());
        }
    }

    private Room findRoomById(int id) {
        if (roomTableView != null && roomTableView.getItems() != null){
            return roomTableView.getItems().stream()
                    .filter(r -> r.getUnitId() == id)
                    .findFirst()
                    .orElse(null);
        }
        return null;
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

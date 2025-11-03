package rentalmanagementsystem.rent_management;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Room {
    private int unitId;
    private String roomNo;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String unitStatus;
    private double price;
    private double areaSize;
    private int capacity;
    private String otp;

    public Room(int unitId, String roomId, LocalDateTime startDate, LocalDateTime endDate, String unitStatus, double price, double areaSize, int capacity, String otp){
        this.unitId = unitId;
        this.roomNo = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.unitStatus = unitStatus;
        this.price = price;
        this.areaSize = areaSize;
        this.capacity = capacity;
        this.otp = otp;
    }

//    getters
    public int getUnitId() {return unitId;}
    public String getRoomNo(){return roomNo;}
    public LocalDateTime getStartDate() {return startDate;}
    public LocalDateTime getEndDate() {return endDate;}
    public String getUnitStatus() {return unitStatus;}
    public Double getPrice() {return price;}
    public Double getAreaSize() {return areaSize;}
    public int getCapacity() {return capacity;}
    public String getOtp() {return otp;}

//    setters
    public void setUnitId(int unitId) {this.unitId = unitId;}
    public void setRoomNo(String roomNo) {this.roomNo = roomNo;}
    public void setStartDate(LocalDateTime startDate) {this.startDate = startDate;}
    public void setEndDate(LocalDateTime endDate) {this.endDate = endDate;}
    public void setUnitStatus(String unitStatus) {this.unitStatus = unitStatus;}
    public void setPrice(double price) {this.price = price;}
    public void setOtp(String otp) {this.otp = otp;}
}

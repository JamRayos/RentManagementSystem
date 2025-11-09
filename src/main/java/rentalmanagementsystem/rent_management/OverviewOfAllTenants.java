package rentalmanagementsystem.rent_management;

public class OverviewOfAllTenants {
    private String name;
    private int unitId;
    private String roomNo;
    private String contactNumber;
    private Double price;
    private String paymentStatus;
    private int capacity;

    OverviewOfAllTenants (String name, int unitId, String roomNo, String contactNumber, double price, String paymentStatus, int capacity) {
        this.name = name;
        this.unitId = unitId;
        this.roomNo = roomNo;
        this.contactNumber = contactNumber;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.capacity = capacity;
    }

    //getters
    public String getName() {return name;}
    public int getUnitId() {return unitId;}
    public String getRoomNo() {return roomNo;}
    public String getContactNumber() {return contactNumber;}
    public Double getPrice() {return price;}
    public String getPaymentStatus() {return paymentStatus;}
    public int getCapacity() {return capacity;}

    //setters
    public void setName (String name) {this.name = name;}
    public void setUnitId (int tenantId) {this.unitId = unitId;}
    public void setRoomNo (String unitNo) {this.roomNo = roomNo;}
    public void setContactNumber (String contactNo) {this.contactNumber = contactNumber;}
    public void setPrice (Double rent) {this.price = price;}
    public void setPaymentStatus (String status) {this.paymentStatus = paymentStatus;}
    public void setCapacity (int noOfTenants) {this.capacity = capacity;}
}

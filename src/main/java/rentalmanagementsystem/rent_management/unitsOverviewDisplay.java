package rentalmanagementsystem.rent_management;

public class unitsOverviewDisplay {
    private String name;
    private int tenantAccountId;
    private String roomNo;
    private String unitStatus;
    private int capacity;
    private double areaSize;

    public unitsOverviewDisplay(String name, int tenantAccountId, String roomNo, String unitStatus, int capacity, double areaSize){
        this.name = name;
        this.tenantAccountId = tenantAccountId;
        this.roomNo = roomNo;
        this.unitStatus = unitStatus;
        this.capacity = capacity;
        this.areaSize = areaSize;
    }

//    getters
    public String getName() {return name;}
    public int getTenantAccountId() {return tenantAccountId;}
    public String getRoomNo() {return roomNo;}
    public String getUnitStatus() {return unitStatus;}
    public int getCapacity() {return capacity;}
    public double getAreaSize() {return areaSize;}

//    setters
    public void setName(String name) {this.name = name;}
    public void setTenantAccountId (int tenantAccountId) {this.tenantAccountId = tenantAccountId;}
    public void setRoomNo (String roomNo) {this.roomNo = roomNo;}
    public void setUnitStatus (String unitStatus) {this.unitStatus = unitStatus;}
    public void setCapacity(int capacity) {this.capacity = capacity;}
    public void setAreaSize(double areaSize) {this.areaSize = areaSize;}
}

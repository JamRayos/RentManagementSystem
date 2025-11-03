package rentalmanagementsystem.rent_management;

import java.time.LocalDateTime;

public class leaseManagementDisplay {

    private String name;
    private int tenantAccountId;
    private String roomNo;
    private String billingPeriod;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public leaseManagementDisplay(String name, int tenantAccountId, String roomNo, String billingPeriod, LocalDateTime startDate, LocalDateTime endDate){
        this.name = name;
        this.tenantAccountId = tenantAccountId;
        this.roomNo = roomNo;
        this.billingPeriod = billingPeriod;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    //        getters
    public String getName() {return name;}
    public int getTenantAccountId() {return tenantAccountId;}
    public String getRoomNo() {return roomNo;}
    public String getBillingPeriod() {return billingPeriod;}
    public LocalDateTime getStartDate() {return startDate;}
    public LocalDateTime getEndDate() {return endDate;}

    //        setters
    public void setName(String name) {this.name = name;}
    public void setTenantAccountId(int tenantAccountId) {this.tenantAccountId = tenantAccountId;}
    public void setRoomNo(String roomNo) {this.roomNo = roomNo;}
    public void setBillingPeriod(String billingPeriod) {this.billingPeriod = billingPeriod;}
    public void setStartDate(LocalDateTime startDate) {this.startDate = startDate;}
    public void setEndDate(LocalDateTime endDate) {this.endDate = endDate;}
}


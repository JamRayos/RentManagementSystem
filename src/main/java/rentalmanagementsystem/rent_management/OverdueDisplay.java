package rentalmanagementsystem.rent_management;

import java.time.LocalDateTime;

public class OverdueDisplay {
    private String name;
    private int tenantAccountId;
    private String roomNo;
    private double overdueBalance;
    private LocalDateTime dueDate;
    private int daysOverdue;
    private boolean archived;

    public OverdueDisplay(String name, int tenantAccountId, String roomNo, double overdueBalance, LocalDateTime dueDate, int daysOverdue, boolean archived) {
        this.name = name;
        this.tenantAccountId = tenantAccountId;
        this.roomNo = roomNo;
        this.overdueBalance = overdueBalance;
        this.dueDate = dueDate;
        this.daysOverdue = daysOverdue;
        this.archived = archived;
    }

//    getters
    public String getName() {return name;}
    public int getTenantAccountId() {return tenantAccountId;}
    public String getRoomNo() {return roomNo;}
    public double getOverdueBalance() {return overdueBalance;}
    public LocalDateTime getDueDate() {return dueDate;}
    public int getDaysOverdue() {return daysOverdue;}
    public boolean getArchived() {return archived;}

//    setters
    public void setName(String name) {this.name = name;}
    public void setTenantAccountId(int tenantAccountId) {this.tenantAccountId = tenantAccountId;}
    public void setRoomNo(String roomNo) {this.roomNo = roomNo;}
    public void setOverdueBalance(double overdueBalance) {this.overdueBalance = overdueBalance;}
    public void setDueDate(LocalDateTime dueDate) {this.dueDate = dueDate;}
    public void setDaysOverdue(int daysOverdue) {this.daysOverdue = daysOverdue;}
    public void setArchived(boolean archived) {this.archived = archived;}

}

package rentalmanagementsystem.rent_management;

public class Billing {
    private int billingId;
    private int unitId;
    private double rentAmount;
    private String billingPeriod;
    private String paymentStatus;

    public Billing(int billingId, int unitId, double rentAmount, String billingPeriod, String paymentStatus){
        this.billingId = billingId;
        this.unitId = unitId;
        this.rentAmount = rentAmount;
        this.billingPeriod = billingPeriod;
        this.paymentStatus = paymentStatus;
    }

//    getters
    public int getBillingId() {return billingId;}
    public int getUnitId() {return unitId;}
    public double getRentAmount() {return rentAmount;}
    public String getBillingPeriod() {return billingPeriod;}
    public String getPaymentStatus() {return paymentStatus;}

//    setters
    public void setBillingId(int billingId) {this.billingId = billingId;}
    public void setUnitId(int unitId) {this.unitId = unitId;}
    public void setRentAmount(double rentAmount) {this.rentAmount = rentAmount;}
    public void setBillingPeriod(String billingPeriod) {this.billingPeriod = billingPeriod;}
    public void setPaymentStatus(String paymentStatus) {this.paymentStatus = paymentStatus;}
}

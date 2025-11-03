package rentalmanagementsystem.rent_management;

import java.time.LocalDateTime;

public class PaymentHistory {
    private int paymentTrackingId;
    private int tenantId;
    private String modeOfPayment;
    private double amountPaid;
    private LocalDateTime paymentDate;
    private String paymentStatus;

    public PaymentHistory(int paymentTrackingId, int tenantId, String modeOfPayment, double amountPaid, LocalDateTime paymentDate, String paymentStatus){
        this.paymentTrackingId = paymentTrackingId;
        this.tenantId = tenantId;
        this.modeOfPayment = modeOfPayment;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentStatus = paymentStatus;
    }

//    getters
    public int getPaymentTrackingId() {return paymentTrackingId;}
    public int getTenantId(){return tenantId;}
    public String getModeOfPayment() {return modeOfPayment;}
    public double getAmountPaid() {return amountPaid;}
    public LocalDateTime getPaymentDate() {return paymentDate;}
    public String getPaymentStatus() {return paymentStatus;}

//    setters
    public void setPaymentTrackingId(int paymentTrackingId) {this.paymentTrackingId = paymentTrackingId;}
    public void setTenantId(int tenantId) {this.tenantId = tenantId;}
    public void setModeOfPayment(String modeOfPayment) {this.modeOfPayment = modeOfPayment;}
    public void setAmountPaid(double amountPaid) {this.amountPaid = amountPaid;}
    public void setPaymentDate(LocalDateTime paymentDate) {this.paymentDate = paymentDate;}
    public void setPaymentStatus(String paymentStatus) {this.paymentStatus = paymentStatus;}
}

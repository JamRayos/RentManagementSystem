package rentalmanagementsystem.rent_management;
import java.time.LocalDateTime;

public class PaymentDisplay {
    private String name;
    private int tenantAccountId;
    private String roomNo;
    private double amountPaid;
    private LocalDateTime paymentDate;
    private String modeOfPayment;

    PaymentDisplay(String name, int tenantAccountId, String roomNo, double amountPaid, LocalDateTime paymentDate, String modeOfPayment){
        this.name = name;
        this.tenantAccountId = tenantAccountId;
        this.roomNo = roomNo;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.modeOfPayment = modeOfPayment;
    }

    //        getters
    public String getName() {return name;}
    public int getTenantAccountId() {return tenantAccountId;}
    public String getRoomNo() {return roomNo;}
    public double getAmountPaid() {return amountPaid;}
    public LocalDateTime getPaymentDate() {return paymentDate;}
    public String getModeOfPayment() {return modeOfPayment;}

    //        setters
    public void setName(String name) {this.name = name;}
    public void setTenantAccountId(int tenantAccountId) {this.tenantAccountId = tenantAccountId;}
    public void setRoomNo(String roomNo) {this.roomNo = roomNo;}
    public void setAmountPaid(double amountPaid) {this.amountPaid = amountPaid;}
    public void setPaymentDate(LocalDateTime paymentDate) {this.paymentDate = paymentDate;}
    public void setModeOfPayment(String modeOfPayment) {this.modeOfPayment = modeOfPayment;}

}
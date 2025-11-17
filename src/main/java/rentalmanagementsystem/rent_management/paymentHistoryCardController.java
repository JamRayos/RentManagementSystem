package rentalmanagementsystem.rent_management;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class paymentHistoryCardController {
    @FXML
    private Label paymentDate;
    @FXML private Label paymentAmount;
    @FXML private Label paymentMethod;

    // Called by the dashboard to populate this card
    public void setData(String date, double amount, String method) {
        paymentDate.setText(date);
        paymentAmount.setText(String.format("PHP %.2f", amount));
        paymentMethod.setText(method);
    }
}

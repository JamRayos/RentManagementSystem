package rentalmanagementsystem.rent_management;

import eu.hansolo.toolbox.time.Times;

import java.sql.*;
import java.time.LocalDateTime;


public class RoomAccountDAO {

    public int linkTenant(Room room, Billing billing){
        String updateRoom = """
                UPDATE roomAccount SET startDate = ?, endDate = ?, unitStatus = 'occupied', otp = ? WHERE unitId = ? AND unitStatus = "vacant"
                """;
        String insertBilling = """
                INSERT INTO billing(unitId, rentAmount, billingPeriod, paymentStatus) VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DbConn.connectDB()){
            conn.setAutoCommit(false);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateRoom);
                PreparedStatement insertStmt = conn.prepareStatement(insertBilling, PreparedStatement.RETURN_GENERATED_KEYS)){

                updateStmt.setTimestamp(1, Timestamp.valueOf(room.getStartDate()));
                updateStmt.setTimestamp(2, Timestamp.valueOf(room.getEndDate()));
                updateStmt.setString(3, room.getOtp());
                updateStmt.setInt(4, room.getUnitId());

                int updated = updateStmt.executeUpdate();
                if(updated == 0){
                    conn.rollback();
                    return 0;
                }

                double rentAmount = computeRent(room.getPrice(), billing.getBillingPeriod());

                insertStmt.setInt(1, room.getUnitId());
                insertStmt.setDouble(2, rentAmount);
                insertStmt.setString(3, billing.getBillingPeriod());
                insertStmt.setString(4, "paid");

                int insertedRows = insertStmt.executeUpdate();

                if (insertedRows > 0){
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()){
                        int billingId = generatedKeys.getInt(1);
                        conn.commit();
                        return billingId;
                    }
                }

                conn.rollback();
                return -1;
            } catch (SQLException e){
                conn.rollback();
                e.printStackTrace();
                return -1;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public double computeRent(double basePrice, String billingPeriod){
        switch (billingPeriod.toLowerCase()){
            case "monthly":
                return basePrice;
            case "quarterly":
                return basePrice * 3;
            case "semi-annual":
                return basePrice * 6;
            case "annual":
                return basePrice * 12;
            default:
                throw new IllegalArgumentException("Unknown billing period");
        }
    }
}

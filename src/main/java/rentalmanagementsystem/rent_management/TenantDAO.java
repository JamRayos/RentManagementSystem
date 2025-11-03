package rentalmanagementsystem.rent_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TenantDAO {

    public int registerTenant(Tenant tenant, String otpCode) {
        String findUnitSql = "SELECT unitId FROM roomAccount WHERE otp = ?";
        String insertTenantSql = """
                INSERT INTO tenantAccount (name, username, email, contactNumber, unitId, password, archived)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String updateUnitSql = "UPDATE roomAccount SET otp = NULL WHERE unitId = ?";

        try (Connection conn = DbConn.connectDB()){
            conn.setAutoCommit(false);

            int unitId = -1;
            try(PreparedStatement findStmt = conn.prepareStatement(findUnitSql)){
                findStmt.setString(1, otpCode);
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()){
                    unitId = rs.getInt("unitId");
                } else {
                    conn.rollback();
                    throw new SQLException("Invalid or expired OTP.");
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertTenantSql, PreparedStatement.RETURN_GENERATED_KEYS)){
                insertStmt.setString(1, tenant.getName());
                insertStmt.setString(2, tenant.getUsername());
                insertStmt.setString(3, tenant.getEmail());
                insertStmt.setString(4, tenant.getContact());
                insertStmt.setInt(5, unitId);
                insertStmt.setString(6, tenant.getPassword());
                insertStmt.setBoolean(7, false);
                insertStmt.executeUpdate();

                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) tenant.setTenantId(keys.getInt(1));
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateUnitSql)){
                updateStmt.setInt(1, unitId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            return tenant.getTenantId();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

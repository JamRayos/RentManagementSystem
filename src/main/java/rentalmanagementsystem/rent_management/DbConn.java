package rentalmanagementsystem.rent_management;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConn {
    public static Connection connectDB() throws SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/rentManagement";
        String user = "root";
        String pass = "MamaPanot11141969:)";

        return DriverManager.getConnection(url, user, pass);
    }
}


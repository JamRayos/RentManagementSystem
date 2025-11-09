package rentalmanagementsystem.rent_management;

public class SessionAdmin {
    private static String currentAdminUsername;

    public static void setCurrentAdmin(String username) {
        currentAdminUsername = username;
    }

    public static String getCurrentAdmin() {
        return currentAdminUsername;
    }

    public static void clear() {
        currentAdminUsername = null;
    }
}

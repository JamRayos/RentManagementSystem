package rentalmanagementsystem.rent_management;

public class SessionLogin {
    private static Tenant currentTenant;

    public static void setCurrentTenant(Tenant tenant) {
        currentTenant = tenant;
    }

    public static Tenant getCurrentTenant() {
        return currentTenant;
    }

    public static void clear() {
        currentTenant = null;
    }
}

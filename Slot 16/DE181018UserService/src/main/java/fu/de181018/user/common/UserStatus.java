package fu.de181018.user.common;

public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED;

    public static boolean isValid(String statusStr) {
        if (statusStr == null) return false;
        for (UserStatus s : values()) {
            if (s.name().equalsIgnoreCase(statusStr.trim())) {
                return true;
            }
        }
        return false;
    }

    public static UserStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        for (UserStatus s : values()) {
            if (s.name().equalsIgnoreCase(statusStr.trim())) {
                return s;
            }
        }
        return null;
    }
}

package fu.de181018.user.common;

public enum Role {
    USER,
    CREATOR,
    ADMIN;

    public static boolean isValid(String roleStr) {
        if (roleStr == null) return false;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return true;
            }
        }
        return false;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return null;
    }
}

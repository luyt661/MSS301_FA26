package fu.de181018.post.common;

public enum PostStatus {
    DRAFT,
    SCHEDULED,
    PUBLISHED,
    CANCELLED;

    public static boolean isValid(String statusStr) {
        if (statusStr == null) return false;
        for (PostStatus s : values()) {
            if (s.name().equalsIgnoreCase(statusStr.trim())) {
                return true;
            }
        }
        return false;
    }

    public static PostStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        for (PostStatus s : values()) {
            if (s.name().equalsIgnoreCase(statusStr.trim())) {
                return s;
            }
        }
        return null;
    }
}

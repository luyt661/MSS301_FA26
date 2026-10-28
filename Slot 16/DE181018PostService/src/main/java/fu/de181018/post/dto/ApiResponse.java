package fu.de181018.post.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ApiResponse {
    private int status;
    private String message;
    private Object data;
    private String timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public ApiResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public static ApiResponse success(int status, String message, Object data) {
        return new ApiResponse(status, message, data);
    }

    public static ApiResponse error(int status, String message) {
        return new ApiResponse(status, message, null);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

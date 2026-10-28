package fu.de181018.post.dto;

public class UserApiResponse {
    private int status;
    private String message;
    private UserDTO data;
    private String timestamp;

    public UserApiResponse() {
    }

    public UserApiResponse(int status, String message, UserDTO data, String timestamp) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
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

    public UserDTO getData() {
        return data;
    }

    public void setData(UserDTO data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

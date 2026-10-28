package fu.de181018.post.common;

import feign.FeignException;
import fu.de181018.post.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiResponse.error(2, ex.getMessage() != null ? ex.getMessage() : "Validation failed"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(4, ex.getMessage() != null ? ex.getMessage() : "Not found"));
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ApiResponse> handleUserNotActiveException(UserNotActiveException ex) {
        // HTTP 400 Bad Request, status = 5 (User is not ACTIVE)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(5, ex.getMessage() != null ? ex.getMessage() : "User is not ACTIVE"));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ApiResponse> handleFeignNotFound(FeignException.NotFound ex) {
        // Requirement 357: catch FeignException.NotFound to return the correct status 4 when user does not exist
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(4, "userId not found"));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse> handleFeignException(FeignException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(0, "Error communicating with UserService: " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiResponse.error(2, "Validation failed"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiResponse.error(2, "Validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(0, "Server error: " + ex.getMessage()));
    }
}

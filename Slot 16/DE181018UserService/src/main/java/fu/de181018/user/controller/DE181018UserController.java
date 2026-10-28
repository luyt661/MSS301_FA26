package fu.de181018.user.controller;

import fu.de181018.user.dto.ApiResponse;
import fu.de181018.user.dto.PageDTO;
import fu.de181018.user.dto.UserDTO;
import fu.de181018.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "DE181018UserController", description = "User Service REST Controller")
public class DE181018UserController {

    private final UserService userService;

    public DE181018UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "1. Create User")
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserDTO userDTO) {
        UserDTO created = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(1, "Created successfully", created));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "2. Update User (partial update)")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable("userId") Long userId,
            @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(ApiResponse.success(1, "Updated successfully", updated));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "3. Get User Detail")
    public ResponseEntity<ApiResponse> getUserDetail(@PathVariable("userId") Long userId) {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(1, "Successful", user));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "4. Deactivate User (delete)")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable("userId") Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponse.success(1, "Set to DEACTIVATED successfully", null));
    }

    @GetMapping
    @Operation(summary = "5. Get User List with pagination & filters")
    public ResponseEntity<ApiResponse> getUserList(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "status", required = false) String status) {
        PageDTO pageDTO = userService.getUserList(page, size, role, status);
        return ResponseEntity.ok(ApiResponse.success(1, "Successful", pageDTO));
    }
}

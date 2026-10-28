package fu.de181018.post.controller;

import fu.de181018.post.dto.ApiResponse;
import fu.de181018.post.dto.PageDTO;
import fu.de181018.post.dto.PostDTO;
import fu.de181018.post.dto.PostDetailDTO;
import fu.de181018.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "DE181018PostController", description = "Post Service REST Controller")
public class DE181018PostController {

    private final PostService postService;

    public DE181018PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "1. Create Post")
    public ResponseEntity<ApiResponse> createPost(@RequestBody PostDTO postDTO) {
        PostDTO created = postService.createPost(postDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(1, "Created successfully", created));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "2. Update Post (partial update)")
    public ResponseEntity<ApiResponse> updatePost(
            @PathVariable("postId") Long postId,
            @RequestBody PostDTO postDTO) {
        PostDTO updated = postService.updatePost(postId, postDTO);
        return ResponseEntity.ok(ApiResponse.success(1, "Updated successfully", updated));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "3. Get Post Detail")
    public ResponseEntity<ApiResponse> getPostDetail(@PathVariable("postId") Long postId) {
        PostDetailDTO detail = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(1, "Successful", detail));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "4. Cancel Post (delete)")
    public ResponseEntity<ApiResponse> cancelPost(@PathVariable("postId") Long postId) {
        postService.cancelPost(postId);
        return ResponseEntity.ok(ApiResponse.success(1, "Cancelled successfully", null));
    }

    @GetMapping
    @Operation(summary = "5. Get Post List with pagination & filters")
    public ResponseEntity<ApiResponse> getPostList(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(name = "advertiserName", required = false) String advertiserName,
            @RequestParam(name = "status", required = false) String status) {
        PageDTO pageDTO = postService.getPostList(page, size, advertiserName, status);
        return ResponseEntity.ok(ApiResponse.success(1, "Successful", pageDTO));
    }
}

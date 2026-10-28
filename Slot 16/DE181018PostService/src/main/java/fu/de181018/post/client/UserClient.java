package fu.de181018.post.client;

import fu.de181018.post.dto.UserApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8081}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserApiResponse getUserById(@PathVariable("userId") Long userId);
}

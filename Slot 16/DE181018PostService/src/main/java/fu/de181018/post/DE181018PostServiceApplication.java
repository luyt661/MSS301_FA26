package fu.de181018.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DE181018PostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DE181018PostServiceApplication.class, args);
    }
}

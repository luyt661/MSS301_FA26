package fu.de181018.post.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI postOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DE181018PostService API")
                        .description("API documentation for PostService - Media Booking Platform")
                        .version("1.0"));
    }
}

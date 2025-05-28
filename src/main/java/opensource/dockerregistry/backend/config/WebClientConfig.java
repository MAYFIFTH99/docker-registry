package opensource.dockerregistry.backend.config;

import java.util.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient registryWebClient() {
        String username = "test";
        String password = "1234";
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        return WebClient.builder()
                .baseUrl("http://localhost:5000") // 배포는 registry:5000 로 변경
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .build();
    }
}

package opensource.dockerregistry.backend.config;

import java.util.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient registryWebClient() {
        String username = "tester";
        String password = "1234";
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        return WebClient.builder()
                .baseUrl("http://registry:5000")
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .build();
    }
}

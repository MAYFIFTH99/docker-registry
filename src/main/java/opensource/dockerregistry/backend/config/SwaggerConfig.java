package opensource.dockerregistry.backend.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi qrustGroupedOpenApi() {
        return GroupedOpenApi
                .builder()
                .group("registry")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.setInfo(new Info()
                                .title("private registry api")
                                .description("사설 도커 레지스트리 API")
                                .version("1.0.0")
                        )
                )
                .build();
    }
}

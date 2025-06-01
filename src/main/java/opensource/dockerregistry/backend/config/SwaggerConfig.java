package opensource.dockerregistry.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // 🔐 Swagger UI → Authorize 버튼을 위한 SecurityScheme 등록
    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "basicAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")))
                .info(new Info()
                        .title("Private Registry API")
                        .description("사설 도커 레지스트리 API 명세서")
                        .version("1.0.0"));
    }

    @Bean
    public GroupedOpenApi registryGroup() {
        return GroupedOpenApi.builder()
                .group("registry")
                .pathsToMatch("/**")
                .build();
    }
}

package com.chatbot.storage.config;

import com.chatbot.storage.infrastructure.security.SecurityProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * The type Open api config.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final SecurityProperties properties;

    /**
     * Custom open api open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RAG Chat Storage API")
                        .version("1.0.0")
                        .description("Production-ready microservice for storing RAG chat histories"))
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(properties.getHeaderName())));
    }
}

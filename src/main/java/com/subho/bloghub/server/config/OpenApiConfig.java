package com.subho.bloghub.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bloghubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BlogHub API")
                        .description("Blog feed, content, social graph, reactions, comments, notifications, and search.")
                        .version("v1"))
                // Documented for forward-compatibility: accessToken is currently threaded
                // through as a plain parameter (no enforcement) per project scope. Once
                // real auth lands, wire this scheme into SecurityConfig as well.
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

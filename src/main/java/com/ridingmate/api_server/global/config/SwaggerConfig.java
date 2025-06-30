package com.ridingmate.api_server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class SwaggerConfig {

    @Value("${swagger.server-url}")
    private String serverUrl;

    private static final String JWT_SCHEME_NAME = "jwtAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(serverUrl())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("Riding Mate API Document")
                .version("1.0")
                .description("");
    }

    private Server serverUrl() {
        return new Server().url(serverUrl);
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(JWT_SCHEME_NAME,
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("Bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(JWT_SCHEME_NAME);
    }
}

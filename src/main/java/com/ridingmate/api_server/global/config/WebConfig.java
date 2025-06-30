package com.ridingmate.api_server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${swagger.server-url}")
    private String serverUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(serverUrl)
                .allowedMethods("*")
                .allowCredentials(true);

        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins(serverUrl)
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
package com.ridingmate.api_server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ors.api-key}")
    private String orsApiKey;

    @Bean
    public WebClient orsWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openrouteservice.org")
                .defaultHeader("Authorization", orsApiKey)
                .build();
    }
}
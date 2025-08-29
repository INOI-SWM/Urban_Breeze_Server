package com.ridingmate.api_server.infra.terra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(TerraProperty.class)
@RequiredArgsConstructor
public class TerraConfig {

    private final TerraProperty terraProperty;

    @Bean
    @Qualifier("terraWebClient")
    public WebClient terraWebClient() {
        return WebClient.builder()
            .baseUrl(terraProperty.baseUrl())
            .defaultHeader("Authorization", terraProperty.apiKey())
            .build();
    }

    @Bean
    public TerraClient terraClient(
        @Qualifier("terraWebClient") WebClient terraWebClient) {
        return new TerraClient(terraWebClient);
    }
}

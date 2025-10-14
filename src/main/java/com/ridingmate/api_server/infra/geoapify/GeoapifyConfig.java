package com.ridingmate.api_server.infra.geoapify;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@EnableConfigurationProperties(GeoapifyProperty.class)
@RequiredArgsConstructor
public class GeoapifyConfig {

    private final GeoapifyProperty geoapifyProperty;

    @Bean
    @Qualifier("geoapifyWebClient")
    public WebClient geoapifyWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(geoapifyProperty.baseUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .build();
    }

    @Bean
    public GeoapifyClient geoapifyClient(
        @Qualifier("geoapifyWebClient") WebClient geoapifyWebClient) {
        return new GeoapifyClient(geoapifyProperty, geoapifyWebClient);
    }
}

package com.ridingmate.api_server.infra.ors;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ors")
public record OrsProperty(
        @NotBlank
        String apiKey,
        @NotBlank
        String baseUrl
) {
} 
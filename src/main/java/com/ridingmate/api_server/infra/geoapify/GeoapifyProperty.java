package com.ridingmate.api_server.infra.geoapify;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geoapify")
public record GeoapifyProperty(
        @NotBlank
        String apikey,
        @NotBlank
        String baseUrl
) {
}

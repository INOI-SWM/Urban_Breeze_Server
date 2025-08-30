package com.ridingmate.api_server.infra.terra;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "terra")
public record TerraProperty(
    @NotBlank
    String developerId,

    @NotBlank
    String apiKey,

    @NotBlank
    String baseUrl,

    List<String> supportedProviders
) {
}

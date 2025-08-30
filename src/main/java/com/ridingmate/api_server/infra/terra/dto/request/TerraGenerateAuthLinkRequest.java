package com.ridingmate.api_server.infra.terra.dto.request;

import java.util.UUID;

public record TerraGenerateAuthLinkRequest(
    String providers,

    String language,

    String reference_id,

    String AuthSuccessRedirectUrl,

    String AuthFailureRedirectUrl
) {
    public static TerraGenerateAuthLinkRequest of(String providers, UUID uuid) {
        return new TerraGenerateAuthLinkRequest(
            providers,
            "en",
            uuid.toString(),
            "https://test",
            "https://test"
        );
    }
}

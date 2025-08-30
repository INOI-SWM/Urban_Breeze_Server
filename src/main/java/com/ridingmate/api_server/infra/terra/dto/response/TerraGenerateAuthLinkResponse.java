package com.ridingmate.api_server.infra.terra.dto.response;

public record TerraGenerateAuthLinkResponse(
    String session_id,

    String url,

    String status,

    Long expiresIn
) {
}

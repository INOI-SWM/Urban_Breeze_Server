package com.ridingmate.api_server.infra.terra;

import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationProviderAuthResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraGenerateAuthLinkResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraProviderAuthResponse;

public class TerraMapper {
    public static IntegrationAuthenticateResponse toIntegrationAuthenticateResponse(TerraGenerateAuthLinkResponse response){
        if (response == null) {
            return null;
        }
        return new IntegrationAuthenticateResponse(response.url());
    }

    public static IntegrationProviderAuthResponse toIntegrationProviderAuthResponse(TerraProviderAuthResponse response){
        if (response == null) {
            return null;
        }
        return new IntegrationProviderAuthResponse(response.authUrl());
    }
}

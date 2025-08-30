package com.ridingmate.api_server.domain.activity.facade;

import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.infra.terra.TerraClient;
import com.ridingmate.api_server.infra.terra.TerraMapper;
import com.ridingmate.api_server.infra.terra.TerraProperty;
import com.ridingmate.api_server.infra.terra.dto.request.TerraGenerateAuthLinkRequest;
import com.ridingmate.api_server.infra.terra.dto.response.TerraGenerateAuthLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationFacade {

    private final TerraClient terraClient;
    private final TerraProperty terraProperty;

    public IntegrationAuthenticateResponse authenticateTerra(AuthUser authUser){
        String providers = String.join(",", terraProperty.supportedProviders());
        TerraGenerateAuthLinkRequest request = TerraGenerateAuthLinkRequest.of(providers, authUser.uuid());
        TerraGenerateAuthLinkResponse terraResponse = terraClient.generateAuthLink(request);
        return TerraMapper.toIntegrationAuthenticateResponse(terraResponse);
    }

}

package com.ridingmate.api_server.domain.activity.facade;

import com.ridingmate.api_server.domain.activity.dto.request.IntegrationProviderAuthRequest;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationProviderAuthResponse;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.service.TerraUserService;
import com.ridingmate.api_server.domain.user.service.UserService;
import com.ridingmate.api_server.global.config.AppConfigProperties;
import com.ridingmate.api_server.infra.terra.TerraClient;
import com.ridingmate.api_server.infra.terra.TerraMapper;
import com.ridingmate.api_server.infra.terra.TerraProperty;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import com.ridingmate.api_server.infra.terra.dto.request.TerraGenerateAuthLinkRequest;
import com.ridingmate.api_server.infra.terra.dto.request.TerraProviderAuthRequest;
import com.ridingmate.api_server.infra.terra.dto.response.TerraGenerateAuthLinkResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraProviderAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationFacade {

    private final TerraClient terraClient;
    private final TerraProperty terraProperty;
    private final AppConfigProperties appConfigProperties;
    private final TerraUserService terraUserService;

    public IntegrationAuthenticateResponse authenticateTerra(AuthUser authUser){
        String providers = String.join(",", terraProperty.supportedProviders());
        TerraGenerateAuthLinkRequest request = TerraGenerateAuthLinkRequest.of(providers, authUser.uuid());
        TerraGenerateAuthLinkResponse terraResponse = terraClient.generateAuthLink(request);
        return TerraMapper.toIntegrationAuthenticateResponse(terraResponse);
    }

    public IntegrationProviderAuthResponse authenticateTerraProvider(AuthUser authUser, TerraProvider terraProvider){
        String appScheme = appConfigProperties.scheme();
        TerraProviderAuthRequest terraRequest = TerraProviderAuthRequest.of(terraProvider.name(), authUser.uuid(), appScheme);
        TerraProviderAuthResponse terraResponse = terraClient.generateProviderAuthLink(terraRequest, terraProvider);

        terraUserService.createTerraUser(authUser.id(), terraResponse.userId() ,terraProvider);

        return TerraMapper.toIntegrationProviderAuthResponse(terraResponse);
    }
}

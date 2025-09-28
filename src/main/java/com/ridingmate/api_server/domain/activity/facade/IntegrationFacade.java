package com.ridingmate.api_server.domain.activity.facade;

import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationProviderAuthResponse;
import com.ridingmate.api_server.domain.activity.dto.response.TerraAuthTokenResponse;
import com.ridingmate.api_server.domain.activity.service.TerraService;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
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
import com.ridingmate.api_server.infra.terra.dto.response.TerraAuthTokenApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationFacade {

    private final TerraClient terraClient;
    private final TerraProperty terraProperty;
    private final AppConfigProperties appConfigProperties;
    private final TerraUserService terraUserService;
    private final UserService userService;
    private final TerraService terraService;

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

    public void getActivities(AuthUser authUser){
        User user = userService.getUser(authUser.id());
        List<TerraUser> terraUsers = terraService.getTerraUsers(user);

        for (TerraUser terraUser : terraUsers){
            LocalDate startDate = terraService.determineActivityStartDate(terraUser);
            terraClient.retrieveActivity(terraUser.getTerraUserId(), startDate);
            terraService.updateLastSyncDate(terraUser);
        }
    }

    /**
     * Terra SDK용 인증 토큰 발급
     * @param authUser 인증된 사용자
     * @return 발급된 Terra 인증 토큰
     */
    public TerraAuthTokenResponse generateTerraAuthToken(AuthUser authUser) {
        try {

            log.info("Terra SDK 인증 토큰 발급 시작: userId={}", authUser.id());
            TerraAuthTokenApiResponse terraResponse = terraClient.generateAuthToken();

            log.info("Terra SDK 인증 토큰 발급 성공: userId={}, token={}",
                    authUser.id(), terraResponse.token());

            return TerraAuthTokenResponse.from(
                    terraResponse.token(),
                    terraResponse.expiresIn(),
                    terraResponse.status()
            );

        } catch (Exception e) {
            log.error("Terra SDK 인증 토큰 발급 실패: userId={}, error={}",
                    authUser.id(), e.getMessage(), e);
            throw new RuntimeException("Terra 인증 토큰 발급에 실패했습니다: " + e.getMessage(), e);
        }
    }
}

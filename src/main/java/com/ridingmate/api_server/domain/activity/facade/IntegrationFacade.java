package com.ridingmate.api_server.domain.activity.facade;

import com.ridingmate.api_server.domain.activity.dto.response.*;
import com.ridingmate.api_server.domain.activity.entity.UserApiUsage;
import com.ridingmate.api_server.domain.activity.service.TerraService;
import com.ridingmate.api_server.domain.activity.service.UserApiUsageService;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.entity.AppleUser;
import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.AppleUserService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationFacade {

    private final TerraClient terraClient;
    private final TerraProperty terraProperty;
    private final AppConfigProperties appConfigProperties;
    private final TerraUserService terraUserService;
    private final AppleUserService appleUserService;
    private final UserService userService;
    private final TerraService terraService;
    private final UserApiUsageService userApiUsageService;

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

    /**
     * 현재 월 API 사용량 조회
     * @param authUser 인증된 사용자
     * @return API 사용량 정보
     */
    public ApiUsageResponse getCurrentMonthUsage(AuthUser authUser) {
        log.info("현재 월 API 사용량 조회: userId={}", authUser.id());
        
        User user = userService.getUser(authUser.id());
        UserApiUsage usage = userApiUsageService.getOrCreateCurrentMonthUsage(user);
        int limit = userApiUsageService.getMonthlyLimit();

        // Terra 연동 정보 조회
        List<TerraUser> terraUsers = terraUserService.getActiveTerraUsers(user);
        List<ProviderSyncInfo> terraProviderInfos = terraUsers.stream()
                .map(ProviderSyncInfo::from)
                .toList();

        // Apple 연동 정보 조회
        List<ProviderSyncInfo> appleProviderInfos = appleUserService.getActiveAppleUsers(user).stream()
                .map(appleUser -> ProviderSyncInfo.fromAppleUser(appleUser))
                .toList();

        // 모든 제공자 정보 합치기
        List<ProviderSyncInfo> providerSyncInfos = new ArrayList<>();
        providerSyncInfos.addAll(terraProviderInfos);
        providerSyncInfos.addAll(appleProviderInfos);
        
        log.info("현재 월 API 사용량 조회 완료: userId={}, currentUsage={}, remaining={}, providerCount={}", 
                authUser.id(), usage.getActivitySyncCount(), limit - usage.getActivitySyncCount(),
                providerSyncInfos.size());
        
        return ApiUsageResponse.of(usage.getActivitySyncCount(), limit, providerSyncInfos);
    }

    /**
     * API 사용량 1회 증가
     * @param authUser 인증된 사용자
     */
    public void incrementApiUsage(AuthUser authUser) {
        log.info("API 사용량 증가: userId={}", authUser.id());
        
        User user = userService.getUser(authUser.id());
        userApiUsageService.incrementApiUsage(user);
        
        log.info("API 사용량 증가 완료: userId={}", authUser.id());
    }

    /**
     * API 사용량 1회 증가 (응답 포함)
     * @param authUser 인증된 사용자
     * @return 증가된 사용량 정보
     */
    public ApiUsageIncrementResponse incrementApiUsageWithResponse(AuthUser authUser) {
        log.info("API 사용량 증가 (응답 포함): userId={}", authUser.id());
        
        User user = userService.getUser(authUser.id());
        UserApiUsage usage = userApiUsageService.incrementApiUsageWithResponse(user);
        int limit = userApiUsageService.getMonthlyLimit();
        ApiUsageIncrementResponse response = ApiUsageIncrementResponse.of(
                usage.getActivitySyncCount(),
                limit
        );
        
        log.info("API 사용량 증가 완료 (응답 포함): userId={}, currentUsage={}, remaining={}", 
                authUser.id(), response.currentUsage(), response.remainingUsage());
        
        return response;
    }

    /**
     * 특정 제공자 연동 해제
     * @param authUser 인증된 사용자
     * @param providerName 제공자 이름
     */
    public void disconnectProvider(AuthUser authUser, String providerName) {
        log.info("특정 제공자 연동 해제: userId={}, providerName={}", authUser.id(), providerName);
        
        User user = userService.getUser(authUser.id());
        terraUserService.disconnectProvider(user, providerName);
        
        log.info("특정 제공자 연동 해제 완료: userId={}, providerName={}", authUser.id(), providerName);
    }
}

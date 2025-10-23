package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.IntegrationProviderAuthRequest;
import com.ridingmate.api_server.domain.activity.dto.response.*;
import com.ridingmate.api_server.domain.activity.exception.IntegrationSuccessCode;
import com.ridingmate.api_server.domain.activity.exception.code.ApiUsageErrorCode;
import com.ridingmate.api_server.domain.activity.facade.IntegrationFacade;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.exception.AppleErrorCode;
import com.ridingmate.api_server.domain.user.exception.UserErrorCode;
import com.ridingmate.api_server.global.exception.ApiErrorCodeExample;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.infra.terra.TerraErrorCode;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
public class IntegrationController implements IntegrationApi{

    private final IntegrationFacade integrationFacade;

    @Override
    @PostMapping("/authentication/widget")
    public ResponseEntity<CommonResponse<IntegrationAuthenticateResponse>> generateTerraWidgetSession(
        @AuthenticationPrincipal AuthUser authUser
        ){
        log.info("[Integration] POST widget session - userId={}",
                authUser != null ? authUser.id() : null);
        IntegrationAuthenticateResponse response = integrationFacade.authenticateTerra(authUser);
        return ResponseEntity
            .status(IntegrationSuccessCode.INTEGRATION_AUTHENTICATION_SUCCESS.getStatus())
            .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_AUTHENTICATION_SUCCESS ,response));
    }

    @Override
    @PostMapping("/authentication")
    @ApiErrorCodeExample(UserErrorCode.class)
    @ApiErrorCodeExample(TerraErrorCode.class)
    public ResponseEntity<CommonResponse<IntegrationProviderAuthResponse>> generateTerraAuthLink(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam TerraProvider terraProvider
            ){
        log.info("[Integration] POST provider auth link - userId={}, provider={}",
                authUser != null ? authUser.id() : null, terraProvider);
        IntegrationProviderAuthResponse response = integrationFacade.authenticateTerraProvider(authUser,
                terraProvider);
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_AUTHENTICATION_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_AUTHENTICATION_SUCCESS, response));
    }

    @Override
    @GetMapping("/activity")
    @ApiErrorCodeExample(UserErrorCode.class)
    @ApiErrorCodeExample(TerraErrorCode.class)
    public ResponseEntity<CommonResponse<Void>> getActivities(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[Integration] GET activities - userId={}",
                authUser != null ? authUser.id() : null);
        integrationFacade.getActivities(authUser);

        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_RETRIEVE_ACTIVITY_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_RETRIEVE_ACTIVITY_SUCCESS));
    }

    @Override
    @PostMapping("/terra/auth-token")
    @ApiErrorCodeExample(UserErrorCode.class)
    @ApiErrorCodeExample(TerraErrorCode.class)
    public ResponseEntity<CommonResponse<TerraAuthTokenResponse>> getTerraAuthToken(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[Integration] POST terra auth token - userId={}",
                authUser != null ? authUser.id() : null);
        TerraAuthTokenResponse response = integrationFacade.generateTerraAuthToken(authUser);

        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_TERRA_AUTH_TOKEN_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_TERRA_AUTH_TOKEN_SUCCESS, response));
    }

    @Override
    @GetMapping("/usage")
    @ApiErrorCodeExample(UserErrorCode.class)
    public ResponseEntity<CommonResponse<ApiUsageResponse>> getApiUsage(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[Integration] GET usage - userId={}",
                authUser != null ? authUser.id() : null);
        ApiUsageResponse response = integrationFacade.getCurrentMonthUsage(authUser);
        
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_API_USAGE_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_API_USAGE_SUCCESS, response));
    }

    @Override
    @PostMapping("/usage/increment")
    @ApiErrorCodeExample(UserErrorCode.class)
    @ApiErrorCodeExample(ApiUsageErrorCode.class)
    public ResponseEntity<CommonResponse<ApiUsageIncrementResponse>> incrementApiUsage(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[Integration] POST usage increment - userId={}",
                authUser != null ? authUser.id() : null);
        ApiUsageIncrementResponse response = integrationFacade.incrementApiUsageWithResponse(authUser);
        
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_API_USAGE_INCREMENT_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_API_USAGE_INCREMENT_SUCCESS, response));
    }

    @Override
    @PostMapping("/apple/connect")
    @ApiErrorCodeExample(UserErrorCode.class)
    @ApiErrorCodeExample(AppleErrorCode.class)
    public ResponseEntity<CommonResponse<AppleConnectResponse>> connectApple(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[Integration] POST apple connect - userId={}",
                authUser != null ? authUser.id() : null);
        AppleConnectResponse response = integrationFacade.connectApple(authUser);
        
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_APPLE_CONNECT_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_APPLE_CONNECT_SUCCESS, response));
    }

    @Override
    @GetMapping("/apple/status")
    @ApiErrorCodeExample(UserErrorCode.class)
    public ResponseEntity<CommonResponse<AppleStatusResponse>> getAppleStatus(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[Integration] GET apple status - userId={}",
                authUser != null ? authUser.id() : null);
        AppleStatusResponse response = integrationFacade.getAppleStatus(authUser);
        
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_APPLE_STATUS_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_APPLE_STATUS_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/provider/{providerName}")
    @ApiErrorCodeExample(UserErrorCode.class)
    @ApiErrorCodeExample(TerraErrorCode.class)
    public ResponseEntity<CommonResponse<Void>> disconnectProvider(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String providerName
    ) {
        log.info("[Integration] DELETE provider - userId={}, providerName={}",
                authUser != null ? authUser.id() : null, providerName);
        integrationFacade.disconnectProvider(authUser, providerName);
        
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_PROVIDER_DISCONNECT_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_PROVIDER_DISCONNECT_SUCCESS));
    }
}

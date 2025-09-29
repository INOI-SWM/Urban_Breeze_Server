package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.IntegrationProviderAuthRequest;
import com.ridingmate.api_server.domain.activity.dto.response.*;
import com.ridingmate.api_server.domain.activity.exception.IntegrationSuccessCode;
import com.ridingmate.api_server.domain.activity.exception.code.ApiUsageErrorCode;
import com.ridingmate.api_server.domain.activity.facade.IntegrationFacade;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.exception.UserErrorCode;
import com.ridingmate.api_server.global.exception.ApiErrorCodeExample;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.infra.terra.TerraErrorCode;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationController implements IntegrationApi{

    private final IntegrationFacade integrationFacade;

    @Override
    @PostMapping("/authentication/widget")
    public ResponseEntity<CommonResponse<IntegrationAuthenticateResponse>> generateTerraWidgetSession(
        @AuthenticationPrincipal AuthUser authUser
        ){
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
        ApiUsageIncrementResponse response = integrationFacade.incrementApiUsageWithResponse(authUser);
        
        return ResponseEntity
                .status(IntegrationSuccessCode.INTEGRATION_API_USAGE_INCREMENT_SUCCESS.getStatus())
                .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_API_USAGE_INCREMENT_SUCCESS, response));
    }
}

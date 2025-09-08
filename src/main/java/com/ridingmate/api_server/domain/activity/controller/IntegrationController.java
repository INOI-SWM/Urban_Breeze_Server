package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.IntegrationProviderAuthRequest;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationProviderAuthResponse;
import com.ridingmate.api_server.domain.activity.exception.IntegrationSuccessCode;
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
}

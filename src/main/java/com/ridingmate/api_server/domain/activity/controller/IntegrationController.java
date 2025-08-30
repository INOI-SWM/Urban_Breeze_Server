package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.exception.IntegrationSuccessCode;
import com.ridingmate.api_server.domain.activity.facade.IntegrationFacade;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.exception.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationController implements IntegrationApi{


    private final IntegrationFacade integrationFacade;

    @Override
    @PostMapping("/authentication")
    public ResponseEntity<CommonResponse<IntegrationAuthenticateResponse>> generateTerraWidgetSession(
        @AuthenticationPrincipal AuthUser authUser
        ){
        IntegrationAuthenticateResponse response = integrationFacade.authenticateTerra(authUser);
        return ResponseEntity
            .status(IntegrationSuccessCode.INTEGRATION_AUTHENTICATION_SUCCESS.getStatus())
            .body(CommonResponse.success(IntegrationSuccessCode.INTEGRATION_AUTHENTICATION_SUCCESS ,response));
    }
}

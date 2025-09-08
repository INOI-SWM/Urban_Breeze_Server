package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.IntegrationProviderAuthRequest;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationProviderAuthResponse;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Integration API", description = "외부 서비스 연동 API")
public interface IntegrationApi {

    @Operation(
            summary = "외부 서비스 연동 인증 위젯 URL 생성",
            description = """
                    Terra와 같은 외부 서비스와의 연동을 시작하기 위한 인증 위젯 URL을 생성하여 반환합니다.
                    
                    - **provider**: 연동할 서비스 제공자 (e.g., STRAVA, GARMIN)
                    - 클라이언트는 이 API를 통해 받은 URL로 사용자를 리디렉션하여 연동 과정을 시작해야 합니다.
                    - 성공적으로 연동이 완료되면, 데이터는 설정된 SQS Destination으로 전송됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 인증 위젯 URL 생성 완료")
    })
    ResponseEntity<CommonResponse<IntegrationAuthenticateResponse>> generateTerraWidgetSession(
            @AuthenticationPrincipal AuthUser authUser
    );

    @Operation(
            summary = "Terra 인증 링크 생성 (Provider 직접 지정)",
            description = """
                    사용자가 특정 데이터 제공사(Fitbit, Garmin 등)의 인증을 직접 시작할 수 있는 URL을 생성하여 반환합니다.
                    
                    - **terraProvider**: `GARMIN`, `FITBIT` 등 연동하려는 외부 서비스 제공자를 직접 지정합니다.
                    - 클라이언트는 반환된 `authUrl`로 사용자를 이동시켜 연동 및 권한 동의 절차를 진행해야 합니다.
                    - 이 방식은 위젯을 거치지 않고 특정 서비스를 바로 연동시킬 때 사용됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 인증 링크 생성 완료"),
            @ApiResponse(responseCode = "400", description = "실패: 지원하지 않는 Provider이거나 잘못된 요청")
    })
    ResponseEntity<CommonResponse<IntegrationProviderAuthResponse>> generateTerraAuthLink(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam TerraProvider terraProvider
            );
}

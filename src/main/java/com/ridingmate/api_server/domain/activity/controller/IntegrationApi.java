package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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

}

package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.IntegrationProviderAuthRequest;
import com.ridingmate.api_server.domain.activity.dto.response.*;
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

    @Operation(
            summary = "연동된 모든 서비스의 활동 기록 동기화 요청",
            description = """
                    현재 사용자와 연동된 모든 외부 서비스(Garmin, Fitbit 등)로부터 활동 기록을 가져오는 **비동기** 작업을 시작합니다.
                    
                    - API는 요청을 접수하면 즉시 **202 Accepted** 응답을 반환하며, 실제 데이터 동기화는 백그라운드에서 수행됩니다.
                    - 동기화 시작 날짜는 서버가 계산합니다.
                      - 사용자가 새로 연동한 서비스가 하나라도 있으면 **최근 30일**의 기록을 가져옵니다.
                      - 모든 서비스가 기존에 동기화 이력이 있다면, **가장 오래된 마지막 동기화 시점**부터 현재까지의 기록을 가져옵니다.
                    - 동기화 완료 여부는 별도의 상태 조회 API나 웹소켓 등을 통해 확인해야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "성공: 동기화 작업이 성공적으로 접수됨"),
            @ApiResponse(responseCode = "404", description = "실패: 사용자에게 연동된 서비스가 하나도 없음")
    })
    ResponseEntity<CommonResponse<Void>> getActivities(@AuthenticationPrincipal AuthUser authUser);

    @Operation(
            summary = "Terra SDK 인증 토큰 발급",
            description = """
                    Terra SDK를 사용하여 삼성 헬스, Apple Health 등과 연동하기 위한 인증 토큰을 발급합니다.
                    
                    - **Terra API 호출**: Terra의 `/v2/auth/generateAuthToken` 엔드포인트를 호출하여 SDK용 토큰을 발급받습니다.
                    - **3분 유효**: 발급된 토큰은 3분(180초) 동안만 유효합니다.
                    - **SDK 연동**: 클라이언트는 이 토큰을 사용하여 Terra SDK의 `initConnection()` 함수를 호출할 수 있습니다.
                    - **자동 갱신**: 토큰이 만료되면 새로운 토큰을 발급받아야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: Terra SDK 인증 토큰 발급 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "Terra API 호출 실패 - 서버 내부 오류")
    })
    ResponseEntity<CommonResponse<TerraAuthTokenResponse>> getTerraAuthToken(@AuthenticationPrincipal AuthUser authUser);

    @Operation(
            summary = "API 사용량 조회",
            description = """
                    현재 사용자의 이번달 API 사용량을 조회합니다.
                    
                    - **현재 사용량**: 이번달 사용한 API 호출 횟수
                    - **월별 제한**: 30회 (운동 기록 동기화만 제한)
                    - **남은 사용량**: 사용 가능한 횟수
                    - **사용률**: 현재 사용량의 비율
                    - **제한 초과 여부**: 제한을 초과했는지 여부
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: API 사용량 조회 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 토큰"),
    })
    ResponseEntity<CommonResponse<ApiUsageResponse>> getApiUsage(@AuthenticationPrincipal AuthUser authUser);

    @Operation(
            summary = "API 사용량 증가",
            description = """
                    현재 사용자의 API 사용량을 1회 증가시킵니다.
                    
                    - **월별 제한**: 30회 (운동 기록 동기화만 제한)
                    - **제한 초과 시**: HTTP 429 (Too Many Requests) 에러 발생
                    - **사용 목적**: Terra API 호출 시 사용량 추적
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: API 사용량 증가 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 토큰"),
            @ApiResponse(responseCode = "429", description = "API 사용량 제한 초과 - 월별 제한(30회) 초과")
    })
    ResponseEntity<CommonResponse<ApiUsageIncrementResponse>> incrementApiUsage(@AuthenticationPrincipal AuthUser authUser);
}

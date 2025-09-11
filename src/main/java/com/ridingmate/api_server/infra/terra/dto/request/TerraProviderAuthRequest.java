package com.ridingmate.api_server.infra.terra.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record TerraProviderAuthRequest(
        @JsonIgnore // provider는 쿼리 파라미터로 사용되므로 JSON 본문에는 포함되지 않도록 설정
        String provider,
        String language,
        @JsonProperty("reference_id") String referenceId,
        @JsonProperty("auth_success_redirect_url") String authSuccessRedirectUrl,
        @JsonProperty("auth_failure_redirect_url") String authFailureRedirectUrl
) {
    /**
     * Terra 인증 요청 DTO를 생성합니다.
     * @param provider 데이터 제공사 (e.g., FITBIT, GARMIN)
     * @param referenceId 우리 시스템의 User UUID
     * @param appScheme application.yml에서 읽어온 Terra 설정값
     * @return TerraProviderAuthRequest
     */
    public static TerraProviderAuthRequest of(String provider, UUID referenceId, String appScheme) {
        // 설정 파일의 스킴을 기반으로 성공/실패 URL을 동적으로 생성
        String successUrl = appScheme + "://auth/success?provider=" + provider.toLowerCase();
        String failureUrl = appScheme + "://auth/failure";

        return new TerraProviderAuthRequest(
                provider,
                "en",
                referenceId.toString(),
                successUrl,
                failureUrl
        );
    }
}

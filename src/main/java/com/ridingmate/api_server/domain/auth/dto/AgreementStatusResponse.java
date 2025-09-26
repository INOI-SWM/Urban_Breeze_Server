package com.ridingmate.api_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 동의항목 상태 응답 DTO
 */
public record AgreementStatusResponse(
        @Schema(description = "서비스 이용약관 동의 여부", example = "true")
        Boolean termsOfServiceAgreed,
        
        @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
        Boolean privacyPolicyAgreed,
        
        @Schema(description = "위치기반 서비스 이용약관 동의 여부", example = "false")
        Boolean locationServiceAgreed,
        
        @Schema(description = "모든 필수 동의항목 동의 여부", example = "false")
        Boolean isCompleted
) {
    public static AgreementStatusResponse from(Boolean termsOfServiceAgreed, 
                                            Boolean privacyPolicyAgreed, 
                                            Boolean locationServiceAgreed) {
        boolean allRequired = termsOfServiceAgreed && privacyPolicyAgreed && locationServiceAgreed;
        
        return new AgreementStatusResponse(
                termsOfServiceAgreed,
                privacyPolicyAgreed,
                locationServiceAgreed,
                allRequired
        );
    }
}

package com.ridingmate.api_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자 동의항목 업데이트 요청 DTO
 */
public record AgreementUpdateRequest(
        @NotNull(message = "서비스 이용약관 동의 여부는 필수입니다")
        @Schema(description = "서비스 이용약관 동의 여부", example = "true")
        Boolean termsOfServiceAgreed,
        
        @NotNull(message = "개인정보 처리방침 동의 여부는 필수입니다")
        @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
        Boolean privacyPolicyAgreed,
        
        @NotNull(message = "위치기반 서비스 이용약관 동의 여부는 필수입니다")
        @Schema(description = "위치기반 서비스 이용약관 동의 여부", example = "true")
        Boolean locationServiceAgreed
) {
}

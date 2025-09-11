package com.ridingmate.api_server.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "한 줄 소개 변경 요청 DTO")
public record IntroduceUpdateRequest(
        @Size(max = 100, message = "한 줄 소개는 100자 이하로 입력해주세요.")
        @Schema(description = "새로운 한 줄 소개", example = "한강에서 주로 라이딩합니다!")
        String introduce
) {
}

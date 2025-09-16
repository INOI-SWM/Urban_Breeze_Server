package com.ridingmate.api_server.domain.activity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "활동 제목 변경 요청")
public record UpdateActivityTitleRequest(
        @Schema(description = "새로운 활동 제목", example = "한강 자전거 라이딩")
        @NotBlank(message = "활동 제목은 필수입니다")
        @Size(max = 100, message = "활동 제목은 100자를 초과할 수 없습니다")
        String title
) {
}

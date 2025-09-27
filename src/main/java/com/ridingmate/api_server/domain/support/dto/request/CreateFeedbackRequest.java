package com.ridingmate.api_server.domain.support.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "피드백 생성 요청")
public record CreateFeedbackRequest(
    @Schema(description = "피드백 내용", example = "주행 기록 저장 시 앱이 종료되는 문제가 있습니다.")
    @NotBlank(message = "피드백 내용은 필수입니다.")
    @Size(max = 2000, message = "피드백 내용은 2000자를 초과할 수 없습니다.")
    String content
) {
}
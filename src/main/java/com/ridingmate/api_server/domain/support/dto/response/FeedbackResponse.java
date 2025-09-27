package com.ridingmate.api_server.domain.support.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ridingmate.api_server.domain.support.entity.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "피드백 응답")
public record FeedbackResponse(
    @Schema(description = "피드백 ID", example = "1")
    Long id,

    @Schema(description = "피드백 내용", example = "주행 기록 저장 시 앱이 종료되는 문제가 있습니다.")
    String content,

    @Schema(description = "생성일", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "수정일", example = "2024-01-15T11:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
            feedback.getId(),
            feedback.getContent(),
            feedback.getCreatedAt(),
            feedback.getUpdatedAt()
        );
    }
}
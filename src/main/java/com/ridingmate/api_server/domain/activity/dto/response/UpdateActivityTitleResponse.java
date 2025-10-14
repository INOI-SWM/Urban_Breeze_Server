package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "활동 제목 변경 응답")
public record UpdateActivityTitleResponse(
        @Schema(description = "활동 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        String activityId,
        
        @Schema(description = "변경된 활동 제목", example = "한강 자전거 라이딩")
        String title
) {
    public static UpdateActivityTitleResponse of(String activityId, String title) {
        return new UpdateActivityTitleResponse(activityId, title);
    }
}

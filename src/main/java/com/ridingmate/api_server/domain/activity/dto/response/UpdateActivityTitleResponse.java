package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "활동 제목 변경 응답")
public record UpdateActivityTitleResponse(
        @Schema(description = "활동 ID", example = "1")
        Long activityId,
        
        @Schema(description = "변경된 활동 제목", example = "한강 자전거 라이딩")
        String title
) {
    public static UpdateActivityTitleResponse of(Long activityId, String title) {
        return new UpdateActivityTitleResponse(activityId, title);
    }
}

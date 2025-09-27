package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.activity.entity.Activity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "활동 목록 항목 응답")
public record ActivityListItemResponse(
        @Schema(description = "활동 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        String activityId,

        @Schema(description = "활동 제목", example = "한강 라이딩")
        String title,

        @Schema(description = "시작 시간", example = "2024-03-15T09:00:00")
        LocalDateTime startedAt,

        @Schema(description = "종료 시간", example = "2024-03-15T11:30:00")
        LocalDateTime endedAt,

        @Schema(description = "총 거리 (km)", example = "25.5")
        Double distance,

        @Schema(description = "소요 시간 (초)", example = "9000")
        Long duration,

        @Schema(description = "상승 고도 (m)", example = "150.0")
        Double elevationGain,

        @Schema(description = "대표 이미지 URL")
        String thumbnailImageUrl,

        @Schema(description = "작성자 프로필 이미지 URL")
        String userProfileImageUrl,

        @Schema(description = "작성자 닉네임", example = "라이더123")
        String userNickname
) {
    /**
     * Activity 엔티티로부터 ActivityListItemResponse 생성
     * @param activity Activity 엔티티
     * @param thumbnailImageUrl 대표 이미지 URL (첫 번째 이미지)
     * @param userProfileImageUrl 사용자 프로필 이미지 URL
     * @return ActivityListItemResponse
     */
    public static ActivityListItemResponse from(Activity activity, 
                                                String thumbnailImageUrl, 
                                                String userProfileImageUrl) {
        return new ActivityListItemResponse(
                activity.getActivityId().toString(),
                activity.getTitle(),
                activity.getStartedAt(),
                activity.getEndedAt(),
                activity.getDistance() / 1000.0, // 미터를 킬로미터로 변환
                activity.getDuration().toSeconds(),
                activity.getElevationGain(),
                thumbnailImageUrl,
                userProfileImageUrl,
                activity.getUser().getNickname()
        );
    }
}

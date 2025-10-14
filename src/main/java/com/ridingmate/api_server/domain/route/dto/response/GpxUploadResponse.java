package com.ridingmate.api_server.domain.route.dto.response;

import com.ridingmate.api_server.domain.route.entity.Recommendation;
import com.ridingmate.api_server.domain.route.entity.Route;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GpxUploadResponse(
        @Schema(description = "생성된 경로 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String routeId,

        @Schema(description = "추천코스 제목", example = "한강 라이딩 코스")
        String title,

        @Schema(description = "추천코스 설명", example = "한강을 따라가는 아름다운 라이딩 코스입니다.")
        String description,

        @Schema(description = "총 거리 (m)", example = "13200")
        Double distanceM,

        @Schema(description = "총 소요 시간 (초)", example = "3600")
        Long durationSeconds,

        @Schema(description = "총 상승 고도 (m)", example = "120.4")
        Double elevationGain,

        @Schema(description = "지역", example = "서울특별시")
        String region,

        @Schema(description = "난이도", example = "보통")
        String difficulty,

        @Schema(description = "경관 타입", example = "강변")
        String landscapeType,

        @Schema(description = "추천 타입", example = "유명 코스")
        String recommendationType,

        @Schema(description = "썸네일 이미지 URL", example = "https://s3.amazonaws.com/bucket/thumbnails/route-1.png")
        String thumbnailImageUrl,

        @Schema(description = "GPX 파일 URL", example = "https://s3.amazonaws.com/bucket/gpx/route-1.gpx")
        String gpxFileUrl,

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    public static GpxUploadResponse from(Route route, Recommendation recommendation, String thumbnailUrl, String gpxFileUrl) {
        return new GpxUploadResponse(
                route.getRouteId().toString(),
                route.getTitle(),
                route.getDescription(),
                route.getDistance(), // km를 m로 변환
                route.getDuration().toSeconds(),
                route.getRoundedElevationGain(),
                route.getRegion().getDisplayName(),
                route.getDifficulty().getDisplayName(),
                route.getLandscapeType().getDisplayName(),
                recommendation.getRecommendationType().getDisplayName(),
                thumbnailUrl,
                gpxFileUrl,
                route.getCreatedAt()
        );
    }
}

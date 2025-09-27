package com.ridingmate.api_server.domain.route.dto.response;

import com.ridingmate.api_server.domain.route.entity.Recommendation;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.global.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecommendationListResponse(
    @Schema(description = "추천 코스 목록")
    List<RecommendationItemResponse> recommendations,
    
    @Schema(description = "페이지네이션 정보")
    PaginationResponse pagination
) {
    
    @Schema(description = "추천 코스 아이템")
    public record RecommendationItemResponse(
        @Schema(description = "경로 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        String routeId,
        
        @Schema(description = "코스 제목", example = "한강 라이딩 코스")
        String title,
        
        @Schema(description = "코스 설명", example = "한강을 따라가는 아름다운 라이딩 코스입니다.")
        String description,
        
        @Schema(description = "총 거리 (km)", example = "13.2")
        Double distanceKm,
        
        @Schema(description = "총 소요 시간 (분)", example = "60")
        Long durationMinutes,
        
        @Schema(description = "총 상승 고도 (m)", example = "120.4")
        Double elevationGain,
        
        @Schema(description = "지역", example = "서울특별시")
        String region,
        
        @Schema(description = "난이도", example = "보통")
        String difficulty,
        
        @Schema(description = "추천 타입", example = "유명 코스")
        String recommendationType,
        
        @Schema(description = "썸네일 이미지 경로", example = "https://s3.amazonaws.com/bucket/thumbnails/route-1.png")
        String thumbnailImagePath
    ) {
        
        public static RecommendationItemResponse from(Route route, Recommendation recommendation, String thumbnailUrl) {
            return new RecommendationItemResponse(
                route.getRouteId().toString(),
                route.getTitle(),
                route.getDescription(),
                route.getDistanceInKm(),
                route.getDuration().toMinutes(),
                route.getRoundedElevationGain(),
                route.getRegion().getDisplayName(),
                route.getDifficulty().getDisplayName(),
                recommendation.getRecommendationType().getDisplayName(),
                thumbnailUrl
            );
        }

    }
}

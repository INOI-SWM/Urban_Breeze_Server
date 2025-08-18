package com.ridingmate.api_server.domain.route.dto.response;

import com.ridingmate.api_server.domain.route.entity.Recommendation;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.enums.RecommendationType;
import com.ridingmate.api_server.global.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "추천 코스 목록 응답")
public record RecommendationListResponse(
    @Schema(description = "추천 코스 목록")
    List<RecommendationItemResponse> recommendations,
    
    @Schema(description = "페이지네이션 정보")
    PaginationResponse pagination
) {
    
    @Schema(description = "추천 코스 아이템")
    public record RecommendationItemResponse(
        @Schema(description = "코스 ID")
        Long id,
        
        @Schema(description = "코스 제목")
        String title,
        
        @Schema(description = "코스 설명")
        String description,
        
        @Schema(description = "총 거리 (km)")
        Double distanceKm,
        
        @Schema(description = "총 소요 시간 (초)")
        Long durationSeconds,
        
        @Schema(description = "총 상승 고도 (m)")
        Double elevationGain,
        
        @Schema(description = "지역")
        String region,
        
        @Schema(description = "난이도")
        String difficulty,
        
        @Schema(description = "추천 타입")
        String recommendationType,
        
        @Schema(description = "썸네일 이미지 경로")
        String thumbnailImagePath
    ) {
        
        public static RecommendationItemResponse from(Route route, Recommendation recommendation, String thumbnailUrl) {
            return new RecommendationItemResponse(
                route.getId(),
                route.getTitle(),
                route.getDescription(),
                route.getDistanceInKm(),
                route.getDuration().getSeconds(),
                route.getRoundedElevationGain(),
                route.getRegion().getDisplayName(),
                route.getDifficulty().getDisplayName(),
                recommendation.getRecommendationType().getDisplayName(),
                thumbnailUrl
            );
        }

    }
} 
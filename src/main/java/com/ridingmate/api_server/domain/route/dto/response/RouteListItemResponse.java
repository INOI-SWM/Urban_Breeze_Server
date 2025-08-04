package com.ridingmate.api_server.domain.route.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ridingmate.api_server.domain.route.entity.Route;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record RouteListItemResponse(
        @Schema(description = "경로 ID", example = "1")
        Long id,

        @Schema(description = "경로 제목", example = "한강 라이딩 경로")
        String title,

        @Schema(description = "썸네일 이미지 URL", example = "https://s3.amazonaws.com/bucket/route-1-thumbnail.jpg")
        String thumbnailUrl,

        @Schema(description = "경로 생성일", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "이동 거리 (km)", example = "13.2")
        Double distance,

        @Schema(description = "총 상승 고도 (m)", example = "120.4")
        Double elevationGain,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "라이더123")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/bucket/profile-1.jpg")
        String profileImageUrl
) {

    /**
     * Route 엔티티와 썸네일 URL, 프로필 이미지 URL로부터 RouteListItemResponse 생성
     * @param route Route 엔티티
     * @param thumbnailUrl 썸네일 이미지 URL
     * @param profileImageUrl 프로필 이미지 URL
     * @return RouteListItemResponse DTO
     */
    public static RouteListItemResponse from(Route route, String thumbnailUrl, String profileImageUrl) {
        return new RouteListItemResponse(
                route.getId(),
                route.getTitle(),
                thumbnailUrl,
                route.getCreatedAt(),
                route.getDistanceInKm(),
                route.getRoundedElevationGain(),
                route.getUser().getId(),
                route.getUser().getNickname(),
                profileImageUrl
        );
    }
} 
package com.ridingmate.api_server.domain.route.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.route.entity.Route;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record RouteDetailResponse(
    @Schema(description = "경로 ID", example = "1")
    Long id,

    @Schema(description = "경로 제목", example = "한강 라이딩 경로")
    String title,

    @Schema(description = "경로 생성일", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "예상 소요시간(min)", example = "71")
    Duration duration,

    @Schema(description = "이동 거리 (km)", example = "13.2")
    Double distance,

    @Schema(description = "총 상승 고도 (m)", example = "120.4")
    Double elevationGain,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "사용자 닉네임", example = "라이더123")
    String nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/bucket/profile-1.jpg")
    String profileImageUrl,

    Integer trackPointsCount,

    List<Point> trackPoints
) {

    public static RouteDetailResponse from(Route route, List<Point> routeGpsPoints){
        return new RouteDetailResponse(
            route.getId(),
            route.getTitle(),
            route.getCreatedAt(),
            route.getDuration(),
            route.getDistance(),
            route.getElevationGain(),
            route.getUser().getId(),
            route.getUser().getNickname(),
            route.getUser().getProfileImagePath(),
            routeGpsPoints.size(),
            routeGpsPoints
        );
    }
}

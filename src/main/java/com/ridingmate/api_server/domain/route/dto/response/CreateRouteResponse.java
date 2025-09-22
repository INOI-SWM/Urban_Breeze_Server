package com.ridingmate.api_server.domain.route.dto.response;

import com.ridingmate.api_server.domain.route.entity.Route;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateRouteResponse(
        @Schema(description = "생성된 경로의 UUID", example = "11d8-eebc-58e0-a7d7-96690800200c9a66")
        String routeId,

        @Schema(description = "경로 제목", example = "한강 라이딩")
        String title,

        @Schema(description = "예상 소요 시간 (분)", example = "60")
        long totalDuration,

        @Schema(description = "총 거리 (km)", example = "13.2")
        double totalDistance,

        @Schema(description = "총 상승 고도 (m)", example = "120.4")
        double totalElevationGain
) {

    public static CreateRouteResponse from(Route route) {
        return new CreateRouteResponse(
                route.getRouteId().toString(),
                route.getTitle(),
                route.getDuration().toMinutes(),
                route.getDistanceInKm(),
                route.getElevationGain()
        );
    }
}

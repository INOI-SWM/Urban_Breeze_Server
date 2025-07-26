package com.ridingmate.api_server.domain.route.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CreateRouteRequest(

        @Schema(description = "경로 제목", example = "한강 라이딩 경로")
        @NotBlank
        String title,

        @Schema(description = "경로 상세 설명", example = "아름다운 한강을 따라 달리는 초심자용 코스입니다.")
        String description,

        @Schema(description = "경로 Polyline", example = "o{~vFf`miWvCkGbAaJjGgQxBwF")
        @NotEmpty
        String polyline,

        @Schema(description = "거리 (미터)", example = "13200.5")
        @NotNull
        Double distance,

        @Schema(description = "예상 소요 시간 (초)", example = "3600")
        @NotNull
        Long duration,

        @Schema(description = "총 상승 고도 (미터)", example = "120.4")
        @NotNull
        Double elevationGain,

        @Schema(description = "평균 경사도", example = "2.5")
        @NotNull
        Double averageGradient,

        @Schema(description = "고도 정보 목록", example = "[20.5, 25.0, 30.2]")
        List<Double> elevations,

        @Schema(
                description = "경로 Bounding Box 좌표 [minLon, minLat, maxLon, maxLat]",
                example = "[127.01, 37.50, 127.05, 37.55]"
        )
        List<Double> bbox
) {}
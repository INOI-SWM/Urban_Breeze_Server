package com.ridingmate.api_server.domain.route.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateRouteRequest(

        @Schema(description = "경로 이름", example = "한강 라이딩 경로")
        @NotBlank
        String name,

        @Schema(description = "Polyline 경로", example = "o{~vFf`miWvCkGbAaJjGgQxBwF")
        @NotEmpty
        String polyline,

        @Schema(description = "거리 (미터)", example = "13200.5")
        @NotNull
        Double distance,

        @Schema(description = "총 소요 시간 (분)", example = "3600")
        @NotNull
        Long duration,

        @Schema(description = "총 상승 고도 (미터)", example = "120.4")
        @NotNull
        Double elevationGain
) {}

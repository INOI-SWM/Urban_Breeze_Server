package com.ridingmate.api_server.domain.route.dto.request;

import com.ridingmate.api_server.infra.ors.dto.request.OrsRouteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RouteSegmentRequest(
        @Schema(
                description = "경로를 구성하는 좌표 리스트 [경도, 위도] 형식",
                example = "[[127.012345, 37.567890], [127.045678, 37.589012]]"
        )
        @NotNull
        List<List<Double>> coordinates,

        @Schema(
                description = "고도 포함 여부",
                example = "true"
        )
        @NotNull
        boolean elevation
) {
    public OrsRouteRequest toOrsRequest() {
        return new OrsRouteRequest(this.coordinates, this.elevation);
    }
}
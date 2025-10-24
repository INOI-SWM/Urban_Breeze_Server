package com.ridingmate.api_server.domain.route.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.entity.RouteGpsLog;
import com.ridingmate.api_server.domain.route.enums.WaypointType;
import com.ridingmate.api_server.global.util.GeometryUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record RouteDetailResponse(
    @Schema(description = "경로 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String routeId,

    @Schema(description = "경로 제목", example = "한강 라이딩 경로")
    String title,

    @Schema(description = "경로 Polyline", example = "o{~vFf`miWvCkGbAaJjGgQxBwF")
    String polyline,

    @Schema(description = "경로 생성일", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "예상 소요시간(초)", example = "4260")
    Long durationSeconds,

    @Schema(description = "이동 거리 (m)", example = "13200")
    Double distanceM,

    @Schema(description = "총 상승 고도 (m)", example = "120.4")
    Double elevationGain,

    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String userId,

    @Schema(description = "사용자 닉네임", example = "라이더123")
    String nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/bucket/profile-1.jpg")
    String profileImageUrl,

    @Schema(description = "샘플링된 경로 좌표 갯수", example = "100")
    Integer trackPointsCount,

    @Schema(description = "샘플링된 경로의 고도 데이터 목록")
    List<ElevationPoint> trackPoints,

    @Schema(
            description = "경로 Bounding Box 좌표 [minLon, minLat, maxLon, maxLat]",
            example = "[127.01, 37.50, 127.05, 37.55]"
    )
    List<Double> bbox
) {

    @Schema(description = "경로 GPS 트랙 포인트 데이터")
    public record ElevationPoint(
            @Schema(description = "데이터 포인트의 인덱스", example = "0")
            long index,
            
            @Schema(description = "경도", example = "126.9780")
            double longitude,
            
            @Schema(description = "위도", example = "37.5665")
            double latitude,
            
            @Schema(description = "해당 지점의 고도 (m)", example = "81.2")
            double elevation,
            
            @Schema(description = "Waypoint 정보 (선택적)")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            WaypointInfo waypoint
    ) {
        @Schema(description = "Waypoint 정보")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record WaypointInfo(
                @Schema(description = "Waypoint 타입", example = "summit")
                WaypointType type,
                
                @Schema(description = "Waypoint 제목 (선택적)", example = "정상")
                @JsonInclude(JsonInclude.Include.NON_NULL)
                String title,
                
                @Schema(description = "Waypoint 설명 (선택적)", example = "산봉우리 정상")
                @JsonInclude(JsonInclude.Include.NON_NULL)
                String description
        ) {}
    }
    
    public static RouteDetailResponse fromWithWaypoints(Route route, List<RouteGpsLog> routeGpsLogs, String profileImageUrl){
        List<ElevationPoint> elevationPoints = IntStream.range(0, routeGpsLogs.size())
                .mapToObj(i -> {
                    RouteGpsLog gpsLog = routeGpsLogs.get(i);
                    
                    // Waypoint 정보 처리
                    ElevationPoint.WaypointInfo waypointInfo = null;
                    if (gpsLog.isWaypoint()) {
                        waypointInfo = new ElevationPoint.WaypointInfo(
                            gpsLog.getWaypointType(),
                            gpsLog.getWaypointTitle(),
                            gpsLog.getWaypointDescription()
                        );
                    }
                    
                    return new ElevationPoint(
                        i,
                        gpsLog.getLongitude(),
                        gpsLog.getLatitude(),
                        gpsLog.getElevation(),
                        waypointInfo
                    );
                })
                .collect(Collectors.toList());

        return new RouteDetailResponse(
            route.getRouteId().toString(),
            route.getTitle(),
            GeometryUtil.lineStringToPolyline(route.getRouteLine()),
            route.getCreatedAt(),
            route.getDuration().toSeconds(),
            route.getDistance(),
            route.getElevationGain(),
            route.getUser().getUuid().toString(),
            route.getUser().getNickname(),
            profileImageUrl,
            elevationPoints.size(),
            elevationPoints,
            List.of(route.getMinLon(), route.getMinLat(), route.getMaxLon(), route.getMaxLat())
        );
    }
}

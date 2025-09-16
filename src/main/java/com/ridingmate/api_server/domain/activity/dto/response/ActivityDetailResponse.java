package com.ridingmate.api_server.domain.activity.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ActivityDetailResponse(
        @Schema(description = "활동 ID", example = "1")
        Long id,

        @Schema(description = "활동 제목", example = "일요일 야간 라이딩")
        String title,

        @Schema(description = "활동 시작 시간", example = "2025-07-06T23:53:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startedAt,

        @Schema(description = "활동 종료 시간", example = "2025-07-07T00:13:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endedAt,

        @Schema(description = "운동 시간 (분)", example = "14")
        Long activeDurationMinutes,

        @Schema(description = "전체 시간 (분)", example = "20")
        Long totalDurationMinutes,

        @Schema(description = "이동 거리 (km)", example = "3.14")
        Double distance,

        @Schema(description = "평균 속도 (km/h)", example = "18.16")
        Double averageSpeed,

        @Schema(description = "상승 고도 (m)", example = "124")
        Double elevationGain,

        @Schema(description = "하강 고도 (m)", example = "124")
        Double elevationLoss,

        @Schema(description = "케이던스", example = "157")
        Integer cadence,

        @Schema(description = "평균 심박수 (bpm)", example = "124")
        Integer averageHeartRate,

        @Schema(description = "최대 심박수 (bpm)", example = "140")
        Integer maxHeartRate,

        @Schema(description = "평균 파워 (W)", example = "--")
        Integer averagePower,

        @Schema(description = "최고 파워 (W)", example = "--")
        Integer maxPower,

        @Schema(description = "사용자 정보")
        UserInfo user,

        @Schema(description = "썸네일 이미지 URL")
        String thumbnailImageUrl,

        @Schema(description = "활동 이미지 목록")
        List<ActivityImageResponse> activityImages,

        @Schema(description = "샘플링된 GPS 좌표 갯수", example = "100")
        Integer trackPointsCount,

        @Schema(description = "샘플링된 GPS 좌표 및 고도 데이터 목록")
        List<TrackPoint> trackPoints,

        @Schema(
                description = "활동 경로 Bounding Box 좌표 [minLon, minLat, maxLon, maxLat]",
                example = "[127.01, 37.50, 127.05, 37.55]"
        )
        List<Double> bbox
) {

    @Schema(description = "활동 GPS 트랙 포인트 데이터")
    public record TrackPoint(
            @Schema(description = "데이터 포인트의 인덱스", example = "0")
            long index,
            @Schema(description = "해당 지점의 고도 (m)", example = "81.2")
            double elevation,
            @Schema(description = "위도", example = "37.5665")
            double latitude,
            @Schema(description = "경도", example = "126.9780")
            double longitude
    ) {}

    @Schema(description = "사용자 정보")
    public record UserInfo(
            @Schema(description = "사용자 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            String uuid,
            @Schema(description = "사용자 닉네임", example = "라이더123")
            String nickname,
            @Schema(description = "프로필 이미지 URL")
            String profileImageUrl
    ) {
        public static UserInfo from(com.ridingmate.api_server.domain.user.entity.User user, String profileImageUrl) {
            return new UserInfo(
                    user.getUuid().toString(),
                    user.getNickname(),
                    profileImageUrl
            );
        }
    }

    @Schema(description = "활동 이미지 정보")
    public record ActivityImageResponse(
            @Schema(description = "이미지 ID", example = "1")
            Long id,
            @Schema(description = "이미지 URL")
            String imageUrl,
            @Schema(description = "표시 순서", example = "1")
            Integer displayOrder
    ) {
        public static ActivityImageResponse from(com.ridingmate.api_server.domain.activity.entity.ActivityImage activityImage, String imageUrl) {
            return new ActivityImageResponse(
                    activityImage.getId(),
                    imageUrl,
                    activityImage.getDisplayOrder()
            );
        }
    }

    public static ActivityDetailResponse from(
            Activity activity,
            List<Point> gpsPoints,
            org.locationtech.jts.geom.Coordinate[] originalCoordinates,
            String profileImageUrl,
            String thumbnailImageUrl,
            List<ActivityImageResponse> activityImages,
            List<Double> bbox
    ) {
        List<TrackPoint> trackPoints = IntStream.range(0, gpsPoints.size())
                .mapToObj(i -> {
                    Point point = gpsPoints.get(i);
                    int originalIndex = (int) point.getX(); // LTTB에서 X는 원본 인덱스
                    org.locationtech.jts.geom.Coordinate originalCoord = originalCoordinates[originalIndex];
                    
                    return new TrackPoint(
                            i, // 다운샘플링된 인덱스
                            point.getY(), // 고도
                            originalCoord.y, // 위도
                            originalCoord.x  // 경도
                    );
                })
                .collect(Collectors.toList());

        Duration totalDuration = Duration.between(activity.getStartedAt(), activity.getEndedAt());
        Duration activeDuration = activity.getDuration();

        // 평균 속도 계산 (km/h)
        double averageSpeed = activeDuration.toMinutes() > 0 
                ? (activity.getDistance() / 1000.0) / (activeDuration.toMinutes() / 60.0)
                : 0.0;

        // 사용자 정보 생성
        UserInfo userInfo = UserInfo.from(activity.getUser(), profileImageUrl);

        return new ActivityDetailResponse(
                activity.getId(),
                activity.getTitle(),
                activity.getStartedAt(),
                activity.getEndedAt(),
                activeDuration.toMinutes(),
                totalDuration.toMinutes(),
                activity.getDistance() / 1000.0, // 미터를 킬로미터로 변환
                Math.round(averageSpeed * 100.0) / 100.0, // 소수점 2자리 반올림
                activity.getElevationGain(),
                activity.getElevationGain(), // TODO: 실제 하강 고도 필드 추가 시 변경
                activity.getCadence(),
                activity.getAverageHeartRate(),
                activity.getMaxHeartRate(),
                activity.getAveragePower(),
                activity.getMaxPower(),
                userInfo,
                thumbnailImageUrl,
                activityImages,
                trackPoints.size(),
                trackPoints,
                bbox
        );
    }
}

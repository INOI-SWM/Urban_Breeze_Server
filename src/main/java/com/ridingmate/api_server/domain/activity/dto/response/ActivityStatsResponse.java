package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.activity.enums.ActivityStatsPeriod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ActivityStatsResponse(
        @Schema(description = "통계 기간 타입", example = "WEEK")
        ActivityStatsPeriod period,

        @Schema(description = "현재 기간 통계")
        CurrentPeriodStats currentPeriod,

        @Schema(description = "기간별 통계 차트 데이터")
        List<PeriodStats> chartData,

        @Schema(description = "전체 통계 요약")
        OverallStats overall
) {

    @Schema(description = "현재 기간 통계")
    public record CurrentPeriodStats(
            @Schema(description = "기간 라벨", example = "25년 7월")
            String periodLabel,

            @Schema(description = "총 라이딩 거리 (km)", example = "3.14")
            Double totalDistance,

            @Schema(description = "총 상승 고도 (m)", example = "124")
            Double totalElevationGain,

            @Schema(description = "총 운동 시간 (초)", example = "5460")
            Long totalDurationSeconds,

            @Schema(description = "라이딩 횟수", example = "1")
            Integer activityCount
    ) {}

    @Schema(description = "기간별 통계 데이터")
    public record PeriodStats(
            @Schema(description = "기간 라벨", example = "6")
            String periodLabel,

            @Schema(description = "시작 날짜")
            LocalDate startDate,

            @Schema(description = "종료 날짜") 
            LocalDate endDate,

            @Schema(description = "총 거리 (km)", example = "3.14")
            Double totalDistance,

            @Schema(description = "총 상승 고도 (m)", example = "124")
            Double totalElevationGain,

            @Schema(description = "총 운동 시간 (초)", example = "5460")
            Long totalDurationSeconds,

            @Schema(description = "활동 횟수", example = "1")
            Integer activityCount
    ) {}

    @Schema(description = "전체 통계 요약")
    public record OverallStats(
            @Schema(description = "총 라이딩 거리 (km)", example = "3.14")
            Double totalDistance,

            @Schema(description = "총 상승 고도 (m)", example = "124") 
            Double totalElevationGain,

            @Schema(description = "총 운동 시간 (초)", example = "5460")
            Long totalDurationSeconds,

            @Schema(description = "총 라이딩 횟수", example = "1")
            Integer totalActivityCount,

            @Schema(description = "첫 번째 활동 날짜")
            LocalDate firstActivityDate,

            @Schema(description = "마지막 활동 날짜")
            LocalDate lastActivityDate
    ) {}
}

package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.activity.enums.ActivityStatsPeriod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ActivityStatsResponse(
        @Schema(description = "통계 기간 정보")
        PeriodInfo period,

        @Schema(description = "기간별 통계 요약")
        SummaryInfo summary,

        @Schema(description = "일별 상세 통계 데이터")
        List<DetailInfo> details,

        @Schema(description = "가장 오래된 운동 기록 일자", example = "2023-01-15")
        LocalDate oldestActivityDate
) {

    @Schema(description = "통계 기간 정보")
    public record PeriodInfo(
            @Schema(description = "기간 타입", example = "week")
            String type,

            @Schema(description = "시작 날짜", example = "2025-06-29")
            LocalDate startDate,

            @Schema(description = "종료 날짜", example = "2025-07-05")
            LocalDate endDate,

            @Schema(description = "표시 제목", example = "25년 7월")
            String displayTitle
    ) {}

    @Schema(description = "기간별 통계 요약")
    public record SummaryInfo(
            @Schema(description = "총 거리 (km)", example = "84.5")
            Double totalDistance,

            @Schema(description = "총 상승 고도 (m)", example = "980")
            Double totalElevationGain,

            @Schema(description = "총 운동 시간 (초)", example = "10860")
            Long totalDurationSeconds,

            @Schema(description = "총 활동 횟수", example = "3")
            Integer totalActivityCount
    ) {}

    @Schema(description = "일별 상세 통계 데이터")
    public record DetailInfo(
            @Schema(description = "라벨 (일/월)", example = "29")
            String label,

            @Schema(description = "해당 일의 통계 값")
            DetailValue value
    ) {}

    @Schema(description = "일별 통계 값")
    public record DetailValue(
            @Schema(description = "거리 (km)", example = "25.2")
            Double distanceKm,

            @Schema(description = "상승 고도 (m)", example = "320")
            Double elevationGainM,

            @Schema(description = "운동 시간 (초)", example = "3600")
            Long durationSeconds
    ) {}

    /**
     * ActivityStatsResponse 생성
     */
    public static ActivityStatsResponse of(PeriodInfo period, SummaryInfo summary, List<DetailInfo> details, LocalDate oldestActivityDate) {
        return new ActivityStatsResponse(period, summary, details, oldestActivityDate);
    }

}

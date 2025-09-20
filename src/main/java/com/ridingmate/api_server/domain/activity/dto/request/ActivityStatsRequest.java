package com.ridingmate.api_server.domain.activity.dto.request;

import com.ridingmate.api_server.domain.activity.enums.ActivityStatsPeriod;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@ParameterObject
public record ActivityStatsRequest(
        @Parameter(
                description = "통계 기간 타입 (WEEK, MONTH, YEAR)",
                example = "WEEK",
                schema = @Schema(defaultValue = "WEEK")
        )
        ActivityStatsPeriod period,
        
        @Parameter(
                description = "조회 시작 날짜",
                example = "2024-09-16"
        )
        LocalDate startDate,
        
        @Parameter(
                description = "조회 종료 날짜",
                example = "2024-09-22"
        )
        LocalDate endDate
) {
    public ActivityStatsRequest {
        if (period == null) period = ActivityStatsPeriod.WEEK;
        if (startDate == null) startDate = LocalDate.now().minusDays(6);
        if (endDate == null) endDate = LocalDate.now();
    }
}

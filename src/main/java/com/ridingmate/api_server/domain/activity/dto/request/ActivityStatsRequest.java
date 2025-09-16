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
                description = "조회할 기준 날짜 (해당 날짜가 포함된 주/월/년의 통계를 조회). 미입력시 오늘 날짜 기준",
                example = "2024-09-16"
        )
        LocalDate targetDate
) {
    public ActivityStatsRequest {
        if (period == null) period = ActivityStatsPeriod.WEEK;
        if (targetDate == null) targetDate = LocalDate.now();
    }
}

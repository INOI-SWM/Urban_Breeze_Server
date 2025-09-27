package com.ridingmate.api_server.domain.activity.dto.projection;

public interface MonthlyStatsProjection {
    Integer getMonth();
    Long getCount();
    Double getTotalDistance();
    Double getTotalElevation();
    Long getTotalDurationSeconds();
}

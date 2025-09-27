package com.ridingmate.api_server.domain.activity.dto.projection;

public interface YearlyStatsProjection {
    Integer getYear();
    Long getCount();
    Double getTotalDistance();
    Double getTotalElevation();
    Long getTotalDurationSeconds();
}

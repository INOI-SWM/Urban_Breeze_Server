package com.ridingmate.api_server.domain.activity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityStatsPeriod {
    WEEK("주간", "주"),
    MONTH("월간", "월"), 
    YEAR("연간", "년");

    private final String description;
    private final String unit;
}

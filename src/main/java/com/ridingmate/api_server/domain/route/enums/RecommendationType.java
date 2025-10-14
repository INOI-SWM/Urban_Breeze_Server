package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "추천 경로 타입")
public enum RecommendationType {
    CROSS_COUNTRY("국토 종주", "국토 종주로 선정된 코스"),
    COMPETITION("대회 코스", "대회에서 주행한 코스"),
    FAMOUS("유명 코스", "자전거인들 사이에서 유명한 코스")
    ;

    @Schema(description = "추천 경로 타입 이름")
    private final String displayName;

    @Schema(description = "추천 경로 타입 설명")
    private final String description;
}

package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "지역")
@Getter
@RequiredArgsConstructor
public enum Region {
    SEOUL("서울시", "서울 지역"),
    GANGWON("강원", "강원도"),
    CHUNGCHEONG("충남", "충청남도"),
    JEOLLA("전남", "전라남도"),
    GYEONGSANG("경남", "경상남도"),
    JEJU("제주", "제주도");

    private final String displayName;
    private final String description;
}

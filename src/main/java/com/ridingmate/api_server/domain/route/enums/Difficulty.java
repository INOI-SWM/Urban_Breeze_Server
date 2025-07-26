package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "경로 난이도")
public enum Difficulty {
    EASY("쉬움"),
    MEDIUM("보통"),
    HARD("어려움")
    ;

    private final String displayName;
}

package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사용자-경로 관계 타입")
public enum RouteRelationType {
    OWNER("소유자", "사용자가 직접 생성한 경로"),
    SHARED("공유받음", "딥링크를 통해 공유받은 경로"),
    ;

    @Schema(description = "관계 타입 이름")
    private final String displayName;
    
    @Schema(description = "관계 타입 설명")
    private final String description;
} 
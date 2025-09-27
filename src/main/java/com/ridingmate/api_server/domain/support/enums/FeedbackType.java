package com.ridingmate.api_server.domain.support.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "피드백 타입")
public enum FeedbackType {
    GENERAL("일반 피드백", "일반적인 의견이나 제안"),
    BUG("버그 신고", "앱의 오류나 문제점 신고"),
    FEATURE_REQUEST("기능 요청", "새로운 기능 추가 요청"),
    UI_UX("UI/UX 개선", "사용자 인터페이스 개선 제안"),
    PERFORMANCE("성능 개선", "앱 성능 관련 피드백"),
    CONTENT("콘텐츠 관련", "경로, 코스 등 콘텐츠 관련 피드백")
    ;

    @Schema(description = "피드백 타입 이름")
    private final String displayName;

    @Schema(description = "피드백 타입 설명")
    private final String description;
}
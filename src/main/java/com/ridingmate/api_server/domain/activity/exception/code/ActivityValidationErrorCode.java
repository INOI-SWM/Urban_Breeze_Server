package com.ridingmate.api_server.domain.activity.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivityValidationErrorCode implements ErrorCode {
    INVALID_ACTIVITY_TITLE(HttpStatus.BAD_REQUEST, "INVALID_ACTIVITY_TITLE", "활동 제목이 유효하지 않습니다."),
    INVALID_ACTIVITY_DATE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_ACTIVITY_DATE_RANGE", "활동 시작 시간이 종료 시간보다 늦을 수 없습니다."),
    INVALID_DISTANCE_VALUE(HttpStatus.BAD_REQUEST, "INVALID_DISTANCE_VALUE", "거리 값이 유효하지 않습니다."),
    INVALID_DURATION_VALUE(HttpStatus.BAD_REQUEST, "INVALID_DURATION_VALUE", "소요 시간 값이 유효하지 않습니다."),
    INVALID_ELEVATION_VALUE(HttpStatus.BAD_REQUEST, "INVALID_ELEVATION_VALUE", "상승 고도 값이 유효하지 않습니다."),
    FUTURE_ACTIVITY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FUTURE_ACTIVITY_NOT_ALLOWED", "미래 시간의 활동은 생성할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

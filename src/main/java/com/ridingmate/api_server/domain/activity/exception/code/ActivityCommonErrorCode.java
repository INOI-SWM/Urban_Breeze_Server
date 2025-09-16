package com.ridingmate.api_server.domain.activity.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivityCommonErrorCode implements ErrorCode {
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND", "활동을 찾을 수 없습니다."),
    ACTIVITY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACTIVITY_ACCESS_DENIED", "해당 활동에 접근 권한이 없습니다."),
    ACTIVITY_ALREADY_EXISTS(HttpStatus.CONFLICT, "ACTIVITY_ALREADY_EXISTS", "이미 존재하는 활동입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

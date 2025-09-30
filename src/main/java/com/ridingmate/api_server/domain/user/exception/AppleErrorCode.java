package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.BaseCode;
import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Apple 연동 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum AppleErrorCode implements ErrorCode {

    APPLE_ALREADY_CONNECTED(HttpStatus.BAD_REQUEST, "APPLE_ALREADY_CONNECTED","이미 Apple HealthKit이 연동되어 있습니다. 기존 연동을 해제한 후 다시 시도해주세요."),
    APPLE_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "APPLE_NOT_CONNECTED","연동된 Apple HealthKit이 없습니다."),
    APPLE_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"APPLE_CONNECTED_FAILED" ,"Apple HealthKit 연동에 실패했습니다."),
    APPLE_DISCONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "APPLE_DISCONNECTION_FAILED","Apple HealthKit 연동 해제에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

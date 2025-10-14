package com.ridingmate.api_server.domain.activity.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API 사용량 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum ApiUsageErrorCode implements ErrorCode {
    API_USAGE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "API_USAGE_LIMIT_EXCEEDED","API 사용량 제한을 초과했습니다. 월별 제한: 30회"),
    ;

    private final HttpStatus status;
    private final String message;
    private final String code;
}
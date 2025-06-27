package com.ridingmate.api_server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_001", "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL_002", "요청 데이터가 유효하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_003", "허용되지 않은 HTTP 메서드입니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "GLOBAL_011", "권한이 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_100", "서버 내부 오류가 발생했습니다."),
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}

package com.ridingmate.api_server.domain.route.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RouteCommonErrorCode implements ErrorCode {
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND", "경로를 찾을 수 없습니다."),
    ROUTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ROUTE_ACCESS_DENIED", "해당 경로에 접근 권한이 없습니다"),
    ROUTE_ALREADY_ADDED(HttpStatus.CONFLICT, "ROUTE_ALREADY_ADDED", "이미 내 경로에 추가된 경로입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

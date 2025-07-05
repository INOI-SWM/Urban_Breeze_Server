package com.ridingmate.api_server.domain.route.exception;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RouteErrorCode implements ErrorCode {
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND", "경로를 찾을 수 없습니다."),
    SHARE_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARE_ID_NOT_FOUND", "경로의 공유 식별자를 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

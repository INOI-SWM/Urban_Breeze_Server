package com.ridingmate.api_server.domain.route.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RouteDetailErrorCode implements ErrorCode {
    ROUTE_GPS_LOGS_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTE_GPS_LOGS_INVALID", "경로의 좌표가 2개 미만으로 불충분합니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

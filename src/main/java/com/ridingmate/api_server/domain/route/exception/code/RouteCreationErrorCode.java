package com.ridingmate.api_server.domain.route.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RouteCreationErrorCode implements ErrorCode {
    ROUTE_POLYLINE_INVALID(HttpStatus.BAD_REQUEST, "ROUTE_POLYLINE_INVALID", "Polyline 형식이 올바르지 않습니다."),
    ROUTE_NOT_ENOUGH_POINTS(HttpStatus.BAD_REQUEST, " ROUTE_NOT_ENOUGH_POINTS", "경로를 생성하려면 최소 2개 이상의 지점이 필요합니다."),
    INVALID_GPS_LOG_COORDINATES(HttpStatus.BAD_REQUEST, "INVALID_GPS_LOG_COORDINATES", "GPS 로그의 좌표와 시간은 필수입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

package com.ridingmate.api_server.infra.geoapify;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeoapifyErrorCode implements ErrorCode {

    /**
     * 우리 서버의 로직 또는 설정 오류로 인해 Geoapify가 4xx 에러를 반환한 경우.
     * 사용자에게는 500 Internal Server Error로 응답해야 함.
     */
    GEOAPIFY_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GEOAPIFY_500_1", "Geoapify API 요청 생성 실패"),

    /**
     * Geoapify 서버가 5xx 에러로 응답한 경우. (Geoapify 서버 내부 오류)
     */
    GEOAPIFY_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "GEOAPIFY_502_1", "Geoapify 서버 내부 오류 발생"),

    /**
     * Geoapify 서버에 연결할 수 없거나 응답 시간 초과 등 네트워크 문제 발생.
     */
    GEOAPIFY_CONNECTION_FAILED(HttpStatus.GATEWAY_TIMEOUT, "GEOAPIFY_504_1", "Geoapify 서버 연결 실패");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

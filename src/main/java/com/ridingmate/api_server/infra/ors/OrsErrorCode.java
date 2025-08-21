package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrsErrorCode implements ErrorCode {

    ORS_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORS_REQUEST_FAILED", "ORS API 요청 생성 실패"),

    /**
     * ORS 서버가 5xx 에러로 응답한 경우. (ORS 서버 내부 오류)
     */
    ORS_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "ORS_SERVER_ERROR", "ORS 서버 내부 오류 발생"),
    /**
     * ORS 서버에 연결할 수 없거나 응답 시간 초과 등 네트워크 문제 발생.
     */
    ORS_CONNECTION_FAILED(HttpStatus.GATEWAY_TIMEOUT, "ORS_CONNECTION_FAILED", "ORS 서버 연결 실패"),

    /**
     * ORS API의 응답은 200 OK였으나, 응답 본문을 파싱(매핑)하는 데 실패한 경우.
     * ORS API의 스펙이 변경되었거나, 예기치 않은 응답이 왔을 때 발생.
     * 사용자에게는 500 Internal Server Error로 응답해야 함.
     */
    ORS_MAPPING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORS_MAPPING_FAILED", "ORS 응답 처리 실패");


    private final HttpStatus status;
    private final String code;
    private final String message;
}

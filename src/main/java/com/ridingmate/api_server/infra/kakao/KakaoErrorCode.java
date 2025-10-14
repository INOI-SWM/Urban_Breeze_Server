package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KakaoErrorCode implements ErrorCode {

    /**
     * 우리 서버의 로직 또는 설정 오류로 인해 Kakao가 4xx 에러를 반환한 경우.
     * 사용자에게는 500 Internal Server Error로 응답해야 함.
     */
    KAKAO_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_REQUEST_FAILED", "Kakao API 요청 생성 실패"),

    /**
     * Kakao 서버가 5xx 에러로 응답한 경우. (Kakao 서버 내부 오류)
     */
    KAKAO_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "KAKAO_SERVER_ERROR", "Kakao 서버 내부 오류 발생"),

    /**
     * Kakao 서버에 연결할 수 없거나 응답 시간 초과 등 네트워크 문제 발생.
     */
    KAKAO_CONNECTION_FAILED(HttpStatus.GATEWAY_TIMEOUT, "KAKAO_CONNECTION_FAILED", "Kakao 서버 연결 실패"),

    /**
     * Kakao API의 응답 데이터는 정상적으로 받았으나, 내용(e.g., 좌표값)이
     * 우리 시스템의 비즈니스 규칙에 맞지 않아 처리에 실패한 경우.
     */
    KAKAO_INVALID_COORDINATE(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_INVALID_COORDINATE", "카카오 응답 데이터의 좌표값이 유효하지 않습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
} 
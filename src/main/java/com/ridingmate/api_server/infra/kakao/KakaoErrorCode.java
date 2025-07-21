package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KakaoErrorCode implements ErrorCode {
    KAKAO_SERVER_CALL_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_SERVER_CALL_FAILED", "외부 카카오 서버와의 통신에 실패했습니다."),
    KAKAO_MAPPING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_MAPPING_FAILED", "카카오 응답 매핑에 실패했습니다."),
    KAKAO_INVALID_COORDINATE(HttpStatus.BAD_GATEWAY, "KAKAO_INVALID_COORDINATE", "카카오 API 응답의 좌표값이 올바르지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
} 
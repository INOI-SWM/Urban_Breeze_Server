package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KakaoErrorCode implements ErrorCode {
    KAKAO_SERVER_CALL_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_502", "외부 카카오 서버와의 통신에 실패했습니다."),
    KAKAO_MAPPING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_002", "카카오 응답 매핑에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
} 
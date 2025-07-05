package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrsErrorCode implements ErrorCode {
    ORS_SERVER_CALL_FAILED(HttpStatus.BAD_GATEWAY, "ORS_502", "외부 ORS 서버와의 통신에 실패했습니다."),
    ORS_MAPPING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORS_002", "ORS 응답 매핑에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

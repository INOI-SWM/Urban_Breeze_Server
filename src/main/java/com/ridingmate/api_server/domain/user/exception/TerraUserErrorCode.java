package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TerraUserErrorCode implements ErrorCode {
    TERRA_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "TERRA_USER_NOT_FOUND", "해당 테라 유저를 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

}

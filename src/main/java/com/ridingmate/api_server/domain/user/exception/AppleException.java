package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.BusinessException;

/**
 * Apple 연동 관련 예외
 */
public class AppleException extends BusinessException {

    public AppleException(AppleErrorCode errorCode) {
        super(errorCode);
    }
}

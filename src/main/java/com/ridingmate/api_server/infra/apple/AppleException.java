package com.ridingmate.api_server.infra.apple;

import com.ridingmate.api_server.global.exception.BusinessException;

/**
 * Apple 관련 예외
 */
public class AppleException extends BusinessException {
    
    public AppleException(AppleErrorCode errorCode) {
        super(errorCode);
    }
}

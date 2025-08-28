package com.ridingmate.api_server.domain.auth.exception;

import com.ridingmate.api_server.global.exception.BusinessException;

public class AuthException extends BusinessException {
    public AuthException (AuthErrorCode errorCode){
        super(errorCode);
    }
}

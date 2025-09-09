package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.BusinessException;

public class TerraUserException extends BusinessException {

    public TerraUserException(TerraUserErrorCode errorCode) { super(errorCode); }

}
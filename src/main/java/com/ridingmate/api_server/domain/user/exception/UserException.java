package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.BusinessException;

public class UserException extends BusinessException {

    public UserException(UserErrorCode errorCode) { super(errorCode); }

}

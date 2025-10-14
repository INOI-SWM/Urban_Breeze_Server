package com.ridingmate.api_server.domain.support.exception;

import com.ridingmate.api_server.domain.support.exception.code.FeedbackErrorCode;
import com.ridingmate.api_server.global.exception.BusinessException;

public class FeedbackException extends BusinessException {

    public FeedbackException(FeedbackErrorCode errorCode) {
        super(errorCode);
    }
}
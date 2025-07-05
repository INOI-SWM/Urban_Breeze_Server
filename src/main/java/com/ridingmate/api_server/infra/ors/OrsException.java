package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.global.exception.BusinessException;

public class OrsException extends BusinessException {

    public OrsException(OrsErrorCode errorCode) {
        super(errorCode);
    }
}

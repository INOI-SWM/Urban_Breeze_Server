package com.ridingmate.api_server.infra.geoapify;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.infra.ors.OrsErrorCode;

public class GeoapifyException extends BusinessException {

    public GeoapifyException(GeoapifyErrorCode errorCode) {
        super(errorCode);
    }
}

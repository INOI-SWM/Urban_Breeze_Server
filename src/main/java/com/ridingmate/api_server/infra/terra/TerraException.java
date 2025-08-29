package com.ridingmate.api_server.infra.terra;


import com.ridingmate.api_server.global.exception.BusinessException;

public class TerraException extends BusinessException {
    public TerraException(TerraErrorCode errorCode) {
        super(errorCode);
    }
}

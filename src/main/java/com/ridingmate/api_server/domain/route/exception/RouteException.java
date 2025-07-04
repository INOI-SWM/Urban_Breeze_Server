package com.ridingmate.api_server.domain.route.exception;

import com.ridingmate.api_server.global.exception.BusinessException;

public class RouteException extends BusinessException {

    public RouteException(RouteErrorCode errorCode) {
        super(errorCode);
    }
}

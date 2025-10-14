package com.ridingmate.api_server.domain.route.exception;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.exception.code.RouteCommonErrorCode;
import com.ridingmate.api_server.domain.route.exception.code.RouteCreationErrorCode;
import com.ridingmate.api_server.domain.route.exception.code.RouteDetailErrorCode;
import com.ridingmate.api_server.domain.route.exception.code.RouteShareErrorCode;
import com.ridingmate.api_server.global.exception.BusinessException;

public class RouteException extends BusinessException {

    public RouteException(RouteCommonErrorCode errorCode) {
        super(errorCode);
    }

    public RouteException(RouteShareErrorCode errorCode){
        super(errorCode);
    }

    public RouteException(RouteCreationErrorCode errorCode){
        super(errorCode);
    }

    public RouteException(RouteDetailErrorCode errorCode){
        super(errorCode);
    }
}

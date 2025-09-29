package com.ridingmate.api_server.domain.activity.exception;

import com.ridingmate.api_server.domain.activity.exception.code.ApiUsageErrorCode;
import com.ridingmate.api_server.global.exception.BusinessException;

/**
 * API 사용량 제한 초과 예외
 */
public class ApiUsageLimitExceededException extends BusinessException {
    
    public ApiUsageLimitExceededException(ApiUsageErrorCode errorCode) {
        super(errorCode);
    }
}
package com.ridingmate.api_server.domain.activity.exception;

import com.ridingmate.api_server.domain.activity.exception.code.ActivityCommonErrorCode;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityImageErrorCode;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityValidationErrorCode;
import com.ridingmate.api_server.global.exception.BusinessException;

public class ActivityException extends BusinessException {

    public ActivityException(ActivityCommonErrorCode errorCode) {
        super(errorCode);
    }

    public ActivityException(ActivityImageErrorCode errorCode) {
        super(errorCode);
    }

    public ActivityException(ActivityValidationErrorCode errorCode) {
        super(errorCode);
    }
}

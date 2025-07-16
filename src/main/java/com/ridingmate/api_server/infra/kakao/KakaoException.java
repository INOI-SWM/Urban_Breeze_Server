package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.global.exception.BusinessException;

public class KakaoException extends BusinessException {

    public KakaoException(KakaoErrorCode errorCode) {
        super(errorCode);
    }
} 
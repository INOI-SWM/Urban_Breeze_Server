package com.ridingmate.api_server.global.config;

import com.ridingmate.api_server.global.util.GpsEncryptionUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * GPS 좌표 자동 암호화/복호화 컨버터
 * JPA Entity의 Double 필드에 @Convert(converter = EncryptedDoubleConverter.class) 추가
 */
@Slf4j
@Component
@Converter
public class EncryptedDoubleConverter implements AttributeConverter<Double, String> {

    private static GpsEncryptionUtil gpsEncryptionUtil;

    @Autowired
    public void setGpsEncryptionUtil(GpsEncryptionUtil gpsEncryptionUtil) {
        EncryptedDoubleConverter.gpsEncryptionUtil = gpsEncryptionUtil;
    }

    @Override
    public String convertToDatabaseColumn(Double attribute) {
        if (attribute == null) {
            return null;
        }
        return gpsEncryptionUtil.encrypt(attribute);
    }

    @Override
    public Double convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return gpsEncryptionUtil.decrypt(dbData);
    }
}


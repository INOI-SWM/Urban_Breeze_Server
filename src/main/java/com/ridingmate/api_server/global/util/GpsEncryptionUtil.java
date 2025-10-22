package com.ridingmate.api_server.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * GPS 좌표 암호화/복호화 유틸리티
 * AES-256-GCM 방식 사용
 */
@Slf4j
@Component
public class GpsEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final SecretKey secretKey;

    public GpsEncryptionUtil(@Value("${gps.encryption.key}") String base64Key) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("암호화 키(Base64)가 유효하지 않습니다.", e);
        }
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("암호화 키는 32바이트(256비트)여야 합니다.");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }
    /**
     * GPS 좌표를 암호화
     * @param value 원본 좌표값 (latitude, longitude, elevation)
     * @return Base64 인코딩된 암호화 문자열
     */
    public String encrypt(Double value) {
        if (value == null) {
            return null;
        }

        try {
            // IV(Initialization Vector) 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // 암호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Double을 바이트 배열로 변환
            byte[] valueBytes = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.doFinal(valueBytes);

            // IV + 암호화된 데이터를 결합
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            // Base64 인코딩
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("GPS 좌표 암호화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("GPS 좌표 암호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 암호화된 GPS 좌표를 복호화
     * @param encryptedValue Base64 인코딩된 암호화 문자열
     * @return 원본 좌표값
     */
    public Double decrypt(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);

            // IV 추출
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            // 암호화된 데이터 추출
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            // 복호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            String valueStr = new String(decrypted, StandardCharsets.UTF_8);

            return Double.parseDouble(valueStr);

        } catch (Exception e) {
            log.error("GPS 좌표 복호화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("GPS 좌표 복호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 좌표값 암호화 (문자열 반환)
     */
    public String encryptCoordinate(Double latitude, Double longitude, Double elevation) {
        if (latitude == null || longitude == null) {
            return null;
        }
        
        String coordinates = String.format("%f,%f,%f", 
                latitude, 
                longitude, 
                elevation != null ? elevation : 0.0);
        
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encrypted = cipher.doFinal(coordinates.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("좌표 암호화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("좌표 암호화 중 오류가 발생했습니다.", e);
        }
    }
}


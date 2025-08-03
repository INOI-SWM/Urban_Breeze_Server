package com.ridingmate.api_server.global.security.provider;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ridingmate.api_server.global.security.dto.GoogleUserInfo;
import com.ridingmate.api_server.global.security.exception.AuthErrorCode;
import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.global.security.config.OAuth2Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class GoogleIdTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenValidator(OAuth2Properties oAuth2Properties) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(oAuth2Properties.getGoogleClientId()))
                .build();
    }

    /**
     * Google ID 토큰을 검증하고 사용자 정보를 추출
     *
     * @param idToken Google ID 토큰
     * @return GoogleUserInfo 사용자 정보
     * @throws BusinessException 토큰 검증 실패 시
     */
    public GoogleUserInfo verify(String idToken) {
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            
            if (googleIdToken == null) {
                log.warn("Google ID 토큰 검증 실패 - 토큰이 유효하지 않음");
                throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
            }

            Payload payload = googleIdToken.getPayload();
            
            // 사용자 정보 추출
            String userId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            log.debug("Google ID 토큰 검증 성공 - 사용자: {}, 이메일: {}", userId, email);

            // 필수 정보 검증
            if (userId == null || userId.isEmpty() || email == null || email.isEmpty()) {
                log.warn("Google ID 토큰에서 필수 정보 누락 - userId: {}, email: {}", userId, email);
                throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
            }

            return GoogleUserInfo.builder()
                    .userId(userId)
                    .email(email)
                    .name(name)  // null 가능
                    .picture(picture)  // null 가능
                    .build();

        } catch (Exception e) {
            log.error("Google ID 토큰 검증 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.TOKEN_VALIDATION_ERROR);
        }
    }
} 
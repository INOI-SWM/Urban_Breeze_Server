package com.ridingmate.api_server.domain.auth.validator;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ridingmate.api_server.domain.auth.config.OAuth2Properties;
import com.ridingmate.api_server.domain.auth.dto.GoogleUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleIdTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenValidator(OAuth2Properties oAuth2Properties) {
        // 다중 Client ID 지원
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(oAuth2Properties.getGoogleClientIds())  // List<String> 사용
                .build();
    }

    /**
     * Google ID 토큰 검증
     *
     * @param idToken Google ID 토큰
     * @return GoogleUserInfo Google 사용자 정보
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    public GoogleUserInfo verify(String idToken) {
        try {
            log.debug("Google ID 토큰 검증 시작");
            
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                log.warn("Google ID 토큰 검증 실패 - 유효하지 않은 토큰");
                throw new IllegalArgumentException("유효하지 않은 Google ID 토큰입니다.");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            
            // 사용자 정보 추출
            GoogleUserInfo userInfo = GoogleUserInfo.builder()
                    .userId(payload.getSubject())
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .picture((String) payload.get("picture"))
                    .build();

            log.debug("Google ID 토큰 검증 성공 - 사용자: {}", userInfo.getEmail());
            return userInfo;

        } catch (Exception e) {
            log.error("Google ID 토큰 검증 중 오류 발생", e);
            throw new IllegalArgumentException("Google ID 토큰 검증에 실패했습니다: " + e.getMessage());
        }
    }
} 
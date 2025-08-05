package com.ridingmate.api_server.global.security.provider;

import com.ridingmate.api_server.global.security.config.OAuth2Properties;
import com.ridingmate.api_server.global.security.dto.KakaoUserInfo;
import com.ridingmate.api_server.infra.kakao.KakaoClient;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Kakao ID 토큰 검증기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoIdTokenValidator {

    private final KakaoClient kakaoClient;

    /**
     * Kakao ID 토큰 검증
     * 
     * @param accessToken Kakao Access Token
     * @return KakaoUserInfo Kakao 사용자 정보
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    public KakaoUserInfo verify(String accessToken) {
        try {
            log.debug("Kakao Access Token 검증 시작");
            
            // Kakao 사용자 정보 API 호출
            KakaoUserInfoResponse userInfoResponse = kakaoClient.getUserInfo(accessToken);
            
            // 사용자 정보 추출
            String userId = String.valueOf(userInfoResponse.id());
            String email = userInfoResponse.kakaoAccount() != null
                ? userInfoResponse.kakaoAccount().email()
                : null;
            
            String nickname = userInfoResponse.properties() != null
                ? userInfoResponse.properties().nickname()
                : "사용자";
            
            String profileImageUrl = userInfoResponse.properties() != null
                ? userInfoResponse.properties().profileImage()
                : null;

            // 사용자 정보 생성
            KakaoUserInfo userInfo = KakaoUserInfo.builder()
                    .userId(userId)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build();

            log.debug("Kakao Access Token 검증 성공 - 사용자: {}", userInfo.getNickname());
            return userInfo;

        } catch (Exception e) {
            log.error("Kakao Access Token 검증 중 오류 발생", e);
            throw new IllegalArgumentException("Kakao Access Token 검증에 실패했습니다: " + e.getMessage());
        }
    }


} 
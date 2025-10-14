package com.ridingmate.api_server.domain.auth.validator;

import com.ridingmate.api_server.domain.auth.dto.KakaoUserInfo;
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
            
            // 카카오 계정 정보 로깅
            log.debug("Kakao API 응답 - ID: {}, kakaoAccount: {}", userId, userInfoResponse.kakaoAccount());
            
            String email = userInfoResponse.kakaoAccount() != null
                ? userInfoResponse.kakaoAccount().email()
                : null;
            
            log.debug("추출된 이메일: {}", email);
            
            // 이메일이 없는 경우 대체 이메일 생성 (카카오 비즈니스 인증 없이는 이메일을 받기 어려움)
            if (email == null || email.isEmpty()) {
                email = "kakao_" + userId + "@kakao.local";
                log.warn("카카오 사용자 이메일 정보 없음 - 대체 이메일 생성: {}", email);
            }
            
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
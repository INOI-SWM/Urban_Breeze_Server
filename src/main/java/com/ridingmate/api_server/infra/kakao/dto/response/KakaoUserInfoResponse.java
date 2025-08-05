package com.ridingmate.api_server.infra.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kakao 사용자 정보 응답 DTO
 */
public record KakaoUserInfoResponse(
    @JsonProperty("id")
    Long id,
    
    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount,
    
    @JsonProperty("properties")
    Properties properties
) {

    /**
     * Kakao 계정 정보
     */
    public record KakaoAccount(
        @JsonProperty("email")
        String email,
        
        @JsonProperty("email_needs_agreement")
        Boolean emailNeedsAgreement,
        
        @JsonProperty("is_email_valid")
        Boolean isEmailValid,
        
        @JsonProperty("is_email_verified")
        Boolean isEmailVerified
    ) {}

    /**
     * 사용자 프로필 정보
     */
    public record Properties(
        @JsonProperty("nickname")
        String nickname,
        
        @JsonProperty("profile_image")
        String profileImage,
        
        @JsonProperty("thumbnail_image")
        String thumbnailImage
    ) {}
} 
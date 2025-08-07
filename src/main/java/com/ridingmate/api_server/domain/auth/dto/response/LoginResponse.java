package com.ridingmate.api_server.domain.auth.dto.response;

import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 응답 DTO
 */
public record LoginResponse(
        @Schema(description = "JWT 토큰 정보")
        TokenInfo tokenInfo,

        @Schema(description = "사용자 정보")
        UserInfo userInfo
) {

    /**
     * 사용자 정보 DTO
     */
    public record UserInfo(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "이메일", example = "user@example.com")
            String email,

            @Schema(description = "닉네임", example = "라이더123")
            String nickname,

            @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/bucket/profile-1.jpg")
            String profileImageUrl
    ) {}

    /**
     * TokenInfo와 UserInfo로부터 LoginResponse 생성
     *
     * @param tokenInfo JWT 토큰 정보
     * @param userInfo 사용자 정보
     * @return LoginResponse
     */
    public static LoginResponse of(TokenInfo tokenInfo, UserInfo userInfo) {
        return new LoginResponse(tokenInfo, userInfo);
    }

    /**
     * TokenInfo와 사용자 정보로부터 LoginResponse 생성
     *
     * @param tokenInfo JWT 토큰 정보
     * @param userId 사용자 ID
     * @param email 이메일
     * @param nickname 닉네임
     * @param profileImageUrl 프로필 이미지 URL
     * @return LoginResponse
     */
    public static LoginResponse of(TokenInfo tokenInfo, Long userId, String email, String nickname, String profileImageUrl) {
        UserInfo userInfo = new UserInfo(userId, email, nickname, profileImageUrl);
        return new LoginResponse(tokenInfo, userInfo);
    }
}
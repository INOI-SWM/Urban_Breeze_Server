package com.ridingmate.api_server.domain.auth.dto.response;

import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.dto.AgreementStatusResponse;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * 로그인 응답 DTO
 */
public record LoginResponse(
        @Schema(description = "JWT 토큰 정보")
        TokenInfo tokenInfo,

        @Schema(description = "사용자 정보")
        UserInfo userInfo,
        
        @Schema(description = "동의항목 상태")
        AgreementStatusResponse agreementStatus
) {

    /**
     * 사용자 정보 DTO
     */
    public record UserInfo(
            @Schema(description = "사용자 고유 식별자(UUID)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            UUID uuid,

            @Schema(description = "닉네임", example = "라이딩메이트")
            String nickname,

            @Schema(description = "이메일 주소", example = "test@ridingmate.com")
            String email,

            @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile.jpg")
            String profileImagePath,

            @Schema(description = "자기소개", example = "한강 라이딩을 즐겨요!")
            String introduce,

            @Schema(description = "출생년도", example = "1990")
            Integer birthYear,

            @Schema(description = "성별", example = "MALE")
            Gender gender
    ) {}

    /**
     * TokenInfo와 UserInfo로부터 LoginResponse 생성
     *
     * @param tokenInfo JWT 토큰 정보
     * @param userInfo 사용자 정보
     * @param agreementStatus 동의항목 상태
     * @return LoginResponse
     */
    public static LoginResponse of(TokenInfo tokenInfo, UserInfo userInfo, AgreementStatusResponse agreementStatus) {
        return new LoginResponse(tokenInfo, userInfo, agreementStatus);
    }

    /**
     * TokenInfo와 사용자 정보로부터 LoginResponse 생성
     *
     * @param tokenInfo JWT 토큰 정보
     * @param user 사용자
     * @param agreementStatus 동의항목 상태
     * @return LoginResponse
     */
    public static LoginResponse of(TokenInfo tokenInfo, User user, AgreementStatusResponse agreementStatus) {
        UserInfo userInfo = new UserInfo(
                user.getUuid(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImagePath(),
                user.getIntroduce(),
                user.getBirthYear(),
                user.getGender()
        );
        return LoginResponse.of(tokenInfo, userInfo, agreementStatus);
    }
}
package com.ridingmate.api_server.domain.user.dto;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "사용자 정보 응답 DTO")
public record UserResponse(
        @Schema(description = "사용자 고유 식별자(UUID)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        UUID uuid,

        @Schema(description = "닉네임", example = "라이딩메이트")
        String nickname,

        @Schema(description = "이메일 주소", example = "test@ridingmate.com")
        String email,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile.jpg")
        String profileImageUrl,

        @Schema(description = "자기소개", example = "한강 라이딩을 즐겨요!")
        String introduce,

        @Schema(description = "출생년도", example = "1990")
        Integer birthYear,

        @Schema(description = "성별", example = "MALE")
        Gender gender
) {
    /**
     * User 엔티티와 프로필 이미지 URL로부터 UserResponse 생성
     */
    public static UserResponse of(User user, String profileImageUrl) {
        return new UserResponse(
                user.getUuid(),
                user.getNickname(),
                user.getEmail(),
                profileImageUrl,
                user.getIntroduce(),
                user.getBirthYear(),
                user.getGender()
        );
    }

    /**
     * User 엔티티로부터 UserResponse 생성 (path 그대로 - 하위 호환용)
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUuid(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImagePath(), // path 그대로
                user.getIntroduce(),
                user.getBirthYear(),
                user.getGender()
        );
    }
}
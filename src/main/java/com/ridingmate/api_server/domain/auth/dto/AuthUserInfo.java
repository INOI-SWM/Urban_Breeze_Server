package com.ridingmate.api_server.domain.auth.dto;

import com.ridingmate.api_server.domain.auth.enums.SocialProvider;
import com.ridingmate.api_server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 로그인 성공 응답 또는 사용자 정보 조회를 위한 DTO (Data Transfer Object)
 * API를 통해 클라이언트에 노출되는 사용자 정보를 담는다.
 */
@Getter
@Builder
public class AuthUserInfo {

    private final Long userId;
    private final UUID uuid;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final SocialProvider provider;
    private final String socialId;

    public static AuthUserInfo from(User user) {
        return AuthUserInfo.builder()
                .userId(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImagePath())
                .provider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .build();
    }
} 
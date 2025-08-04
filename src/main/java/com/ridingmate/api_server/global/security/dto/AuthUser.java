package com.ridingmate.api_server.global.security.dto;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.security.enums.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private Long userId;

    private String email;

    private String nickname;

    private String profileImageUrl;

    private SocialProvider provider;
    
    /**
     * 해당 소셜 제공자의 고유 ID
     * - Google: sub 값
     * - Kakao: id 값  
     * - Naver: id 값
     * - Apple: sub 값
     */
    private String socialId;
    
    /**
     * Spring Security에서 사용하는 권한 정보
     * 현재는 모든 사용자에게 ROLE_USER 권한 부여
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * User 엔티티로부터 AuthUser 생성
     *
     * @param user 사용자 엔티티
     * @return AuthUser 인증 사용자 정보
     */
    public static AuthUser from(User user) {
        return AuthUser.builder()
                .userId(user.getId())
                .provider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImagePath())
                .build();
    }
} 
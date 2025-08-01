package com.ridingmate.api_server.global.security.dto;

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
    
    /**
     * 사용자 ID (DB의 Primary Key)
     */
    private Long userId;
    
    /**
     * 사용자 이메일
     */
    private String email;
    
    /**
     * 사용자 닉네임  
     */
    private String nickname;
    
    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;
    
    // ===== 소셜 로그인 관련 =====
    
    /**
     * 소셜 로그인 제공자 (GOOGLE, KAKAO, NAVER, APPLE)
     */
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
} 
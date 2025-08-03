package com.ridingmate.api_server.domain.user.entity;

   import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.route.entity.UserRoute;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import com.ridingmate.api_server.global.security.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_path")
    private String profileImagePath;




        @Builder
    public User(SocialProvider socialProvider, String socialId, String email, 
                String nickname, String profileImagePath) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImagePath = profileImagePath;
    }


        /**
     * 소셜 로그인 정보로 사용자 생성
     */
    public static User createFromSocialLogin(SocialProvider provider, String socialId, 
                                           String email, String nickname, String profileImagePath) {
        return User.builder()
                .socialProvider(provider)
                .socialId(socialId)
                .email(email)
                .nickname(nickname)
                .profileImagePath(profileImagePath)
                .build();
    }
}

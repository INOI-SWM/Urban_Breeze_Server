package com.ridingmate.api_server.domain.user.entity;

import com.ridingmate.api_server.domain.user.Gender;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import com.ridingmate.api_server.domain.auth.enums.SocialProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider")
    private SocialProvider socialProvider;

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "introduce")
    private String introduce;

    @Min(1900)
    @Max(2025)
    @Column(name = "birth_year")
    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Builder
    public User(SocialProvider socialProvider, String socialId, String email,
                String nickname, String profileImagePath) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImagePath = profileImagePath;
    }

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
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

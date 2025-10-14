package com.ridingmate.api_server.domain.user.entity;

import com.ridingmate.api_server.domain.user.enums.Gender;
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

    public static final String DEFAULT_PROFILE_IMAGE_PATH = "profile/default.png";

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

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_path", columnDefinition = "VARCHAR(255) DEFAULT 'profile/default.png'")
    private String profileImagePath = DEFAULT_PROFILE_IMAGE_PATH;

    @Column(name = "introduce")
    private String introduce;

    @Min(1900)
    @Max(2025)
    @Column(name = "birth_year")
    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "terms_of_service_agreed", nullable = false)
    private Boolean termsOfServiceAgreed;

    @Column(name = "privacy_policy_agreed", nullable = false)
    private Boolean privacyPolicyAgreed;

    @Column(name = "location_service_agreed", nullable = false)
    private Boolean locationServiceAgreed;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(SocialProvider socialProvider, String socialId, String email,
                String nickname, String profileImagePath, Boolean termsOfServiceAgreed,
                Boolean privacyPolicyAgreed, Boolean locationServiceAgreed) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImagePath = profileImagePath;
        this.termsOfServiceAgreed = termsOfServiceAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.locationServiceAgreed = locationServiceAgreed;
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
                                           String email, String nickname) {
        return User.builder()
                .socialProvider(provider)
                .socialId(socialId)
                .email(email)
                .nickname(nickname)
                .profileImagePath(DEFAULT_PROFILE_IMAGE_PATH)
                .termsOfServiceAgreed(false)      // 서비스 이용약관 동의 (필수)
                .privacyPolicyAgreed(false)       // 개인정보 처리방침 동의 (필수)
                .locationServiceAgreed(false)     // 위치기반 서비스 이용약관 동의 (필수)
                .build();
    }

    /**
     * 닉네임 업데이트
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 한 줄 소개 업데이트
     */
    public void updateIntroduce(String introduce) {
        this.introduce = introduce;
    }

    /**
     * 성별 업데이트
     */
    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * 출생년도 업데이트
     */
    public void updateBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    /**
     * 프로필 이미지 경로 업데이트
     */
    public void updateProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    /**
     * 서비스 이용약관 동의 업데이트
     */
    public void updateTermsOfServiceAgreed(Boolean termsOfServiceAgreed) {
        this.termsOfServiceAgreed = termsOfServiceAgreed;
    }

    /**
     * 개인정보 처리방침 동의 업데이트
     */
    public void updatePrivacyPolicyAgreed(Boolean privacyPolicyAgreed) {
        this.privacyPolicyAgreed = privacyPolicyAgreed;
    }

    /**
     * 위치기반 서비스 이용약관 동의 업데이트
     */
    public void updateLocationServiceAgreed(Boolean locationServiceAgreed) {
        this.locationServiceAgreed = locationServiceAgreed;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 사용자 삭제 처리 (소프트 삭제)
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        maskPersonalData();
    }

    /**
     * 개인정보 마스킹 처리
     */
    private void maskPersonalData() {
        this.email = "deleted_" + this.id + "@deleted.com";
        this.nickname = "탈퇴한 사용자";
        this.profileImagePath = DEFAULT_PROFILE_IMAGE_PATH;
        this.introduce = null;
    }

}

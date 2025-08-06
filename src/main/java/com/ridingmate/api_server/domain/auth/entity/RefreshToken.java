package com.ridingmate.api_server.domain.auth.entity;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token", columnList = "token"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Refresh Token 값
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false;

    @Column(name = "family_id", nullable = false)
    private String familyId;

    @Column(name = "previous_token_id")
    private Long previousTokenId;

    @Builder
    private RefreshToken(String token, User user, LocalDateTime expiresAt, 
                        String familyId, Long previousTokenId) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.familyId = familyId;
        this.previousTokenId = previousTokenId;
        this.isUsed = false;
        this.isRevoked = false;
    }

    /**
     * 토큰 사용 처리
     */
    public void markAsUsed() {
        this.isUsed = true;
    }

    /**
     * 토큰 무효화 처리
     */
    public void revoke() {
        this.isRevoked = true;
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean isValid() {
        return !isUsed && !isRevoked && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
} 
package com.ridingmate.api_server.domain.auth.repository;

import com.ridingmate.api_server.domain.auth.entity.RefreshToken;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token Repository
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰으로 RefreshToken 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자의 유효한 RefreshToken 목록 조회
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.isUsed = false AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 사용자의 모든 RefreshToken 무효화
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    void revokeAllByUser(@Param("user") User user);

    /**
     * 패밀리 ID로 모든 RefreshToken 무효화 (보안 위반 시)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.familyId = :familyId")
    void revokeAllByFamilyId(@Param("familyId") String familyId);

    /**
     * 만료된 토큰 삭제 (정리 작업용)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expirationTime")
    void deleteExpiredTokens(@Param("expirationTime") LocalDateTime expirationTime);

    /**
     * 사용자의 토큰 개수 조회
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.isUsed = false AND rt.isRevoked = false AND rt.expiresAt > :now")
    long countValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 패밀리 ID로 RefreshToken 목록 조회
     */
    List<RefreshToken> findByFamilyIdOrderByCreatedAtDesc(String familyId);
} 
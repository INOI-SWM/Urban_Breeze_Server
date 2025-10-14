package com.ridingmate.api_server.domain.auth.service;

import com.ridingmate.api_server.domain.auth.entity.RefreshToken;
import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.repository.RefreshTokenRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.domain.auth.config.JwtProperties;
import com.ridingmate.api_server.domain.auth.dto.AuthUserInfo;
import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * 새로운 Refresh Token 생성 및 저장
     *
     * @param user 사용자
     * @param familyId 토큰 패밀리 ID (최초 생성 시 null)
     * @param previousTokenId 이전 토큰 ID (최초 생성 시 null)
     * @return RefreshToken 엔티티
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, String familyId, Long previousTokenId) {
        // 패밀리 ID가 없으면 새로 생성
        if (familyId == null) {
            familyId = UUID.randomUUID().toString();
        }

        // JWT Refresh Token 생성
        String jwtToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 만료 시간 계산
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpireTime() / 1000);

        // RefreshToken 엔티티 생성
        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtToken)
                .user(user)
                .expiresAt(expiresAt)
                .familyId(familyId)
                .previousTokenId(previousTokenId)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("새로운 Refresh Token 생성 완료 - 사용자: {}, 패밀리: {}", user.getId(), familyId);
        
        return saved;
    }

    /**
     * Refresh Token 검증 및 새로운 토큰 발급 (Token Rotation)
     *
     * @param refreshTokenValue Refresh Token 문자열
     * @return TokenInfo 새로운 토큰 정보
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    @Transactional
    public TokenInfo refreshAccessToken(String refreshTokenValue) {
        // 1. Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // 2. 토큰 유효성 검증
        validateRefreshToken(refreshToken);

        // 3. 기존 토큰 사용 처리
        refreshToken.markAsUsed();

        // 4. 새로운 Access Token 생성
        TokenInfo newTokenInfo = jwtTokenProvider.generateTokenInfo(AuthUserInfo.from(refreshToken.getUser()));

        // 5. 새로운 Refresh Token 생성 (Token Rotation)
        RefreshToken newRefreshToken = createRefreshToken(
                refreshToken.getUser(),
                refreshToken.getFamilyId(),
                refreshToken.getId()
        );

        // 6. 새로운 TokenInfo 반환 (새로운 Refresh Token 포함)
        TokenInfo rotatedTokenInfo = TokenInfo.bearer(
                newTokenInfo.accessToken(),
                newRefreshToken.getToken(),
                newTokenInfo.expiresIn()
        );

        log.info("토큰 갱신 완료 - 사용자: {}, 패밀리: {}", 
                refreshToken.getUser().getId(), refreshToken.getFamilyId());

        return rotatedTokenInfo;
    }

    /**
     * Refresh Token 유효성 검증
     *
     * @param refreshToken RefreshToken 엔티티
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    private void validateRefreshToken(RefreshToken refreshToken) {
        // 토큰 무효화 여부 확인
        if (refreshToken.isRevoked()) {
            log.warn("무효화된 Refresh Token 사용 시도 - 패밀리: {}", refreshToken.getFamilyId());
            // 보안 위반 - 패밀리 전체 무효화
            revokeTokenFamily(refreshToken.getFamilyId());
            throw new BusinessException(AuthErrorCode.REVOKED_REFRESH_TOKEN);
        }

        // 토큰 사용 여부 확인
        if (refreshToken.isUsed()) {
            log.warn("이미 사용된 Refresh Token 재사용 시도 - 패밀리: {}", refreshToken.getFamilyId());
            // 보안 위반 - 패밀리 전체 무효화
            revokeTokenFamily(refreshToken.getFamilyId());
            throw new BusinessException(AuthErrorCode.USED_REFRESH_TOKEN);
        }

        // 토큰 만료 여부 확인
        if (refreshToken.isExpired()) {
            log.debug("만료된 Refresh Token - ID: {}", refreshToken.getId());
            throw new BusinessException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // JWT 자체 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken.getToken())) {
            log.warn("JWT 검증 실패 - Refresh Token ID: {}", refreshToken.getId());
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    /**
     * 토큰 패밀리 전체 무효화 (보안 위반 시)
     *
     * @param familyId 패밀리 ID
     */
    @Transactional
    public void revokeTokenFamily(String familyId) {
        refreshTokenRepository.revokeAllByFamilyId(familyId);
        log.warn("토큰 패밀리 전체 무효화 완료 - 패밀리: {}", familyId);
    }

    /**
     * 사용자의 모든 Refresh Token 무효화 (로그아웃 시)
     *
     * @param user 사용자
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("사용자 모든 토큰 무효화 완료 - 사용자: {}", user.getId());
    }

    /**
     * 만료된 토큰 정리 (스케줄링용)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7); // 7일 전 토큰 삭제
        refreshTokenRepository.deleteExpiredTokens(cutoff);
        log.info("만료된 Refresh Token 정리 완료 - 기준일: {}", cutoff);
    }
} 
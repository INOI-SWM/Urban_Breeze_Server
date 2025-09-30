package com.ridingmate.api_server.domain.user.service;

import com.ridingmate.api_server.domain.user.entity.AppleUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.exception.AppleErrorCode;
import com.ridingmate.api_server.domain.user.exception.AppleException;
import com.ridingmate.api_server.domain.user.repository.AppleUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Apple 연동 사용자 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleUserService {

    private final AppleUserRepository appleUserRepository;

    /**
     * Apple 연동 조회 또는 생성 (getOrCreate 패턴)
     */
    @Transactional
    public AppleUser getOrCreateAppleUser(User user) {
        log.info("Apple 연동 조회 또는 생성: userId={}", user.getId());

        // 기존 활성화된 연동이 있으면 반환
        Optional<AppleUser> existingActiveAppleUser = appleUserRepository.findByUserAndIsActiveTrue(user);
        if (existingActiveAppleUser.isPresent()) {
            log.info("기존 Apple 연동 반환: userId={}, appleUserEntityId={}", 
                    user.getId(), existingActiveAppleUser.get().getId());
            return existingActiveAppleUser.get();
        }

        // 새로운 Apple 연동 생성
        AppleUser appleUser = AppleUser.builder()
                .user(user)
                .isActive(true)
                .build();

        log.info("새 Apple 연동 생성 완료: userId={}, appleUserEntityId={}",
                user.getId(), appleUser.getId());

        return appleUserRepository.save(appleUser);
    }

    /**
     * Apple 연동 강제 생성 (중복 시 오류)
     */
    @Transactional
    public AppleUser createAppleUser(User user) {
        log.info("Apple 연동 강제 생성: userId={}", user.getId());

        // 기존 활성화된 연동이 있으면 오류
        Optional<AppleUser> existingActiveAppleUser = appleUserRepository.findByUserAndIsActiveTrue(user);
        if (existingActiveAppleUser.isPresent()) {
            log.warn("이미 Apple HealthKit이 연동되어 있음: userId={}", user.getId());
            throw new AppleException(AppleErrorCode.APPLE_ALREADY_CONNECTED);
        }

        // 새로운 Apple 연동 생성
        AppleUser appleUser = AppleUser.builder()
                .user(user)
                .isActive(true)
                .build();

        AppleUser savedAppleUser = appleUserRepository.save(appleUser);
        log.info("Apple 연동 강제 생성 완료: userId={}, appleUserEntityId={}", 
                user.getId(), savedAppleUser.getId());

        return savedAppleUser;
    }

    /**
     * 사용자의 활성화된 Apple 연동 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AppleUser> getActiveAppleUsers(User user) {
        log.info("활성화된 Apple 연동 목록 조회: userId={}", user.getId());
        return appleUserRepository.findAllByUserAndIsActiveTrue(user);
    }

    /**
     * 사용자의 모든 Apple 연동 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AppleUser> getAllAppleUsers(User user) {
        log.info("모든 Apple 연동 목록 조회: userId={}", user.getId());
        return appleUserRepository.findAllByUser(user);
    }

    /**
     * 사용자의 활성화된 Apple 연동 정보 조회
     */
    @Transactional(readOnly = true)
    public Optional<AppleUser> getActiveAppleUser(User user) {
        log.info("사용자의 활성화된 Apple 연동 정보 조회: userId={}", user.getId());
        return appleUserRepository.findByUserAndIsActiveTrue(user);
    }

    /**
     * 사용자의 활성화된 Apple 연동 정보 조회 (없으면 Exception 발생)
     */
    @Transactional(readOnly = true)
    public AppleUser getAppleUser(User user) {
        log.info("사용자의 Apple 연동 정보 조회: userId={}", user.getId());
        return appleUserRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new AppleException(AppleErrorCode.APPLE_NOT_CONNECTED));
    }

    /**
     * Apple 연동 해제
     */
    @Transactional
    public void disconnectAppleUser(User user) {
        log.info("Apple 연동 해제: userId={}", user.getId());

        AppleUser appleUser = appleUserRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new AppleException(AppleErrorCode.APPLE_NOT_CONNECTED));

        appleUser.deactivate();
        appleUserRepository.save(appleUser);

        log.info("Apple 연동 해제 완료: userId={}", user.getId());
    }

    /**
     * 마지막 동기화 시간 업데이트
     */
    @Transactional
    public void updateLastSyncDate(AppleUser appleUser) {
        log.info("Apple 연동 마지막 동기화 시간 업데이트: appleUserId={}", appleUser.getId());

        appleUser.updateLastSyncDate();
        appleUserRepository.save(appleUser);

        log.info("Apple 연동 마지막 동기화 시간 업데이트 완료: appleUserId={}", appleUser.getId());
    }

    /**
     * 사용자의 Apple 연동 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasActiveAppleUser(User user) {
        log.info("Apple 연동 여부 확인: userId={}", user.getId());
        return appleUserRepository.existsByUserAndIsActiveTrue(user);
    }

    /**
     * 사용자의 Apple 연동 개수 조회
     */
    @Transactional(readOnly = true)
    public long getActiveAppleUserCount(User user) {
        log.info("활성화된 Apple 연동 개수 조회: userId={}", user.getId());
        return appleUserRepository.countByUserAndIsActiveTrue(user);
    }

    /**
     * 사용자의 가장 최근 Apple 연동 정보 조회
     */
    @Transactional(readOnly = true)
    public Optional<AppleUser> getMostRecentAppleUser(User user) {
        log.info("가장 최근 Apple 연동 정보 조회: userId={}", user.getId());
        return appleUserRepository.findMostRecentByUser(user);
    }

    /**
     * 특정 기간 내에 동기화된 Apple 연동 정보 조회
     */
    @Transactional(readOnly = true)
    public List<AppleUser> getActiveAppleUsersAfterDate(User user, LocalDateTime startDate) {
        log.info("특정 기간 내 Apple 연동 정보 조회: userId={}, startDate={}", user.getId(), startDate);
        return appleUserRepository.findActiveByUserAndLastSyncDateAfter(user, startDate);
    }
}

package com.ridingmate.api_server.domain.activity.service;

import com.ridingmate.api_server.domain.activity.entity.UserApiUsage;
import com.ridingmate.api_server.domain.activity.repository.UserApiUsageRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 사용자 API 사용량 관리 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserApiUsageService {

    private final UserApiUsageRepository userApiUsageRepository;
    
    private static final int MONTHLY_LIMIT = 30;

    /**
     * 현재 월 사용량 조회 또는 생성
     * @param user 사용자
     * @return 현재 월 사용량 정보
     */
    public UserApiUsage getOrCreateCurrentMonthUsage(User user) {
        return userApiUsageRepository.findByUserAndCurrentMonth(user)
                .orElseGet(() -> {
                    UserApiUsage newUsage = createForCurrentMonth(user);
                    return userApiUsageRepository.save(newUsage);
                });
    }

    /**
     * 현재 년월로 UserApiUsage 생성
     */
    public UserApiUsage createForCurrentMonth(User user) {
        LocalDate now = LocalDate.now();
        return UserApiUsage.builder()
                .user(user)
                .year(now.getYear())
                .month(now.getMonthValue())
                .activitySyncCount(0)
                .build();
    }

    public int getMonthlyLimit(){
        return MONTHLY_LIMIT;
    }
}
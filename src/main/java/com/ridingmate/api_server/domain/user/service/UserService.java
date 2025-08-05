package com.ridingmate.api_server.domain.user.service;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.security.dto.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 조회 또는 생성
     *
     * @param socialUserInfo 소셜 사용자 정보
     * @return User 사용자 엔티티
     */
    @Transactional
    public User findOrCreateUser(SocialUserInfo socialUserInfo) {
        // 기존 사용자 조회
        return userRepository.findBySocialProviderAndSocialId(
                socialUserInfo.getProvider(), 
                socialUserInfo.getSocialId()
        ).orElseGet(() -> {
            // 새 사용자 생성
            User newUser = User.createFromSocialLogin(
                    socialUserInfo.getProvider(),
                    socialUserInfo.getSocialId(),
                    socialUserInfo.getEmail(),
                    socialUserInfo.getNickname(),
                    socialUserInfo.getProfileImageUrl()
            );
            
            User savedUser = userRepository.save(newUser);
            log.info("새 사용자 생성 - ID: {}, 이메일: {}", savedUser.getId(), savedUser.getEmail());
            
            return savedUser;
        });
    }
} 
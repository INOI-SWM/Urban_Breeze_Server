package com.ridingmate.api_server.domain.user.service;

import com.ridingmate.api_server.domain.auth.dto.SocialUserInfo;
import com.ridingmate.api_server.domain.user.dto.request.BirthYearUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.GenderUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.IntroduceUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.NicknameUpdateRequest;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.exception.UserErrorCode;
import com.ridingmate.api_server.domain.user.exception.UserException;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
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
                    socialUserInfo.getNickname()
            );

            User savedUser = userRepository.save(newUser);
            log.info("새 사용자 생성 - ID: {}, 이메일: {}", savedUser.getId(), savedUser.getEmail());

            return savedUser;
        });
    }

    public User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User getMyInfo(Long userId) {
        return getUser(userId);
    }

    public User updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = getUser(userId);
        user.updateNickname(request.nickname());
        return user;
    }

    public User updateIntroduce(Long userId, IntroduceUpdateRequest request) {
        User user = getUser(userId);
        user.updateIntroduce(request.introduce());
        return user;
    }

    public User updateGender(Long userId, GenderUpdateRequest request) {
        User user = getUser(userId);
        user.updateGender(request.gender());
        return user;
    }

    public User updateBirthYear(Long userId, BirthYearUpdateRequest request) {
        User user = getUser(userId);
        user.updateBirthYear(request.birthYear());
        return user;
    }
} 
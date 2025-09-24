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
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final S3Manager s3Manager;

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

    public User updateProfileImage(Long userId, MultipartFile profileImage) {
        User user = getUser(userId);
        
        // 파일이 없거나 비어있으면 기본 이미지로 변경
        if (profileImage == null || profileImage.isEmpty()) {
            return resetProfileImageToDefault(user);
        }
        
        // 파일 유효성 검증
        validateProfileImage(profileImage);
        
        // 기존 프로필 이미지가 기본값이 아닌 경우 S3에서 삭제
        String currentImagePath = user.getProfileImagePath();
        if (currentImagePath != null && !currentImagePath.equals(User.DEFAULT_PROFILE_IMAGE_PATH)) {
            try {
                s3Manager.deleteFile(currentImagePath);
                log.info("기존 프로필 이미지 삭제 완료: {}", currentImagePath);
            } catch (Exception e) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", currentImagePath, e);
            }
        }
        
        // 새 프로필 이미지를 S3에 업로드
        String imagePath = "profile/" + user.getUuid() + "_" + System.currentTimeMillis() + getFileExtension(profileImage.getOriginalFilename());
        s3Manager.uploadFile(imagePath, profileImage);
        
        // 사용자 엔티티의 프로필 이미지 경로 업데이트
        user.updateProfileImagePath(imagePath);
        
        log.info("사용자 {} 프로필 이미지 업데이트 완료: {}", userId, imagePath);
        return user;
    }
    
    /**
     * 프로필 이미지를 기본값으로 리셋
     */
    private User resetProfileImageToDefault(User user) {
        // 기존 프로필 이미지가 기본값이 아닌 경우 S3에서 삭제
        String currentImagePath = user.getProfileImagePath();
        if (currentImagePath != null && !currentImagePath.equals(User.DEFAULT_PROFILE_IMAGE_PATH)) {
            try {
                s3Manager.deleteFile(currentImagePath);
                log.info("기존 프로필 이미지 삭제 완료: {}", currentImagePath);
            } catch (Exception e) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", currentImagePath, e);
            }
        }
        
        // 기본 이미지로 설정
        user.updateProfileImagePath(User.DEFAULT_PROFILE_IMAGE_PATH);
        
        log.info("사용자 {} 프로필 이미지를 기본값으로 리셋 완료", user.getId());
        return user;
    }
    
    /**
     * 프로필 이미지 파일 유효성 검증
     */
    private void validateProfileImage(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new UserException(UserErrorCode.INVALID_PROFILE_IMAGE);
        }
        
        // 파일 크기 확인 (20MB 제한)
        long maxSize = 20 * 1024 * 1024; // 20MB
        if (file.getSize() > maxSize) {
            throw new UserException(UserErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }
        
        // 파일 형식 확인 (이미지 파일만 허용)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UserException(UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT);
        }
        
        // 허용된 이미지 형식 확인
        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/webp"};
        boolean isAllowedType = false;
        for (String allowedType : allowedTypes) {
            if (allowedType.equals(contentType)) {
                isAllowedType = true;
                break;
            }
        }
        
        if (!isAllowedType) {
            throw new UserException(UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT);
        }
    }
    
    /**
     * 파일 확장자 추출 (기본값: .jpg)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".jpg";
        }
        return filename.substring(lastDotIndex);
    }
} 
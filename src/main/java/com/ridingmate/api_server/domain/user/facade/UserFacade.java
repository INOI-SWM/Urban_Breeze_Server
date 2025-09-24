package com.ridingmate.api_server.domain.user.facade;

import com.ridingmate.api_server.domain.auth.dto.AgreementStatusResponse;
import com.ridingmate.api_server.domain.auth.dto.AgreementUpdateRequest;
import com.ridingmate.api_server.domain.auth.service.AgreementService;
import com.ridingmate.api_server.domain.user.dto.UserResponse;
import com.ridingmate.api_server.domain.user.dto.request.BirthYearUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.GenderUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.IntroduceUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.NicknameUpdateRequest;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final S3Manager s3Manager;
    private final AgreementService agreementService;

    public UserResponse getMyInfo(Long userId) {
        User user = userService.getMyInfo(userId);
        String profileImageUrl = s3Manager.getPresignedUrl(user.getProfileImagePath());
        return UserResponse.of(user, profileImageUrl);
    }

    public UserResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = userService.updateNickname(userId, request);
        String profileImageUrl = s3Manager.getPresignedUrl(user.getProfileImagePath());
        return UserResponse.of(user, profileImageUrl);
    }

    public UserResponse updateIntroduce(Long userId, IntroduceUpdateRequest request) {
        User user = userService.updateIntroduce(userId, request);
        String profileImageUrl = s3Manager.getPresignedUrl(user.getProfileImagePath());
        return UserResponse.of(user, profileImageUrl);
    }

    public UserResponse updateGender(Long userId, GenderUpdateRequest request) {
        User user = userService.updateGender(userId, request);
        String profileImageUrl = s3Manager.getPresignedUrl(user.getProfileImagePath());
        return UserResponse.of(user, profileImageUrl);
    }

    public UserResponse updateBirthYear(Long userId, BirthYearUpdateRequest request) {
        User user = userService.updateBirthYear(userId, request);
        String profileImageUrl = s3Manager.getPresignedUrl(user.getProfileImagePath());
        return UserResponse.of(user, profileImageUrl);
    }

    public UserResponse updateProfileImage(Long userId, MultipartFile profileImage) {
        User user = userService.updateProfileImage(userId, profileImage);
        String profileImageUrl = s3Manager.getPresignedUrl(user.getProfileImagePath());
        return UserResponse.of(user, profileImageUrl);
    }

    public AgreementStatusResponse updateAgreements(Long userId, AgreementUpdateRequest request) {
        return agreementService.updateAgreements(userId, request);
    }

    /**
     * 사용자 삭제 처리
     */
    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }
}

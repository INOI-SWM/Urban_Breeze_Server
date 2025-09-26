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
import com.ridingmate.api_server.domain.user.service.TerraUserService;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.domain.activity.service.ActivityService;
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
    private final RouteService routeService;
    private final ActivityService activityService;
    private final TerraUserService terraUserService;

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

    public UserResponse deleteProfileImage(Long userId) {
        User user = userService.deleteProfileImage(userId);
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
        // 1. 사용자 삭제 (소프트 삭제 + 개인정보 마스킹)
        User user = userService.getUser(userId);
        userService.deleteUser(user);
        
        // 2. 관련 데이터 처리 (각 서비스에 위임)
        try {
            routeService.handleUserDeletion(user);
        } catch (Exception e) {
            // 경로 데이터 처리 실패해도 사용자 삭제는 계속 진행
        }
        
        try {
            activityService.handleUserDeletion(user);
        } catch (Exception e) {
            // 활동 데이터 처리 실패해도 사용자 삭제는 계속 진행
        }
        
        try {
            terraUserService.handleUserDeletion(user);
        } catch (Exception e) {
            // Terra 연동 해제 실패해도 사용자 삭제는 계속 진행
        }
    }
}

package com.ridingmate.api_server.domain.user.facade;

import com.ridingmate.api_server.domain.user.dto.UserResponse;
import com.ridingmate.api_server.domain.user.dto.request.BirthYearUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.GenderUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.IntroduceUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.NicknameUpdateRequest;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    public UserResponse getMyInfo(Long userId) {
        User user = userService.getMyInfo(userId);
        return UserResponse.from(user);
    }

    public UserResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = userService.updateNickname(userId, request);
        return UserResponse.from(user);
    }

    public UserResponse updateIntroduce(Long userId, IntroduceUpdateRequest request) {
        User user = userService.updateIntroduce(userId, request);
        return UserResponse.from(user);
    }

    public UserResponse updateGender(Long userId, GenderUpdateRequest request) {
        User user = userService.updateGender(userId, request);
        return UserResponse.from(user);
    }

    public UserResponse updateBirthYear(Long userId, BirthYearUpdateRequest request) {
        User user = userService.updateBirthYear(userId, request);
        return UserResponse.from(user);
    }

    public UserResponse updateProfileImage(Long userId, MultipartFile profileImage) {
        User user = userService.updateProfileImage(userId, profileImage);
        return UserResponse.from(user);
    }
}

package com.ridingmate.api_server.domain.user.controller;

import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.dto.UserResponse;
import com.ridingmate.api_server.domain.user.dto.request.BirthYearUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.GenderUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.IntroduceUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.NicknameUpdateRequest;
import com.ridingmate.api_server.domain.user.exception.UserSuccessCode;
import com.ridingmate.api_server.domain.user.facade.UserFacade;
import com.ridingmate.api_server.global.exception.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController implements UserApi {

    private final UserFacade userFacade;

    @Override
    @GetMapping("/user/profile")
    public ResponseEntity<CommonResponse<UserResponse>> getMyInfo(AuthUser authUser) {
        UserResponse response = userFacade.getMyInfo(authUser.id());
        return ResponseEntity
                .status(UserSuccessCode.GET_MY_INFO_SUCCESS.getStatus())
                .body(CommonResponse.success(UserSuccessCode.GET_MY_INFO_SUCCESS, response));
    }

    @Override
    @PutMapping("/user/profile/nickname")
    public ResponseEntity<CommonResponse<UserResponse>> updateNickname(AuthUser authUser, NicknameUpdateRequest request) {
        UserResponse response = userFacade.updateNickname(authUser.id(), request);
        return ResponseEntity
                .status(UserSuccessCode.UPDATE_NICKNAME_SUCCESS.getStatus())
                .body(CommonResponse.success(UserSuccessCode.UPDATE_NICKNAME_SUCCESS, response));
    }

    @Override
    @PutMapping("/user/profile/introduce")
    public ResponseEntity<CommonResponse<UserResponse>> updateIntroduce(AuthUser authUser, IntroduceUpdateRequest request) {
        UserResponse response = userFacade.updateIntroduce(authUser.id(), request);
        return ResponseEntity
                .status(UserSuccessCode.UPDATE_INTRODUCE_SUCCESS.getStatus())
                .body(CommonResponse.success(UserSuccessCode.UPDATE_INTRODUCE_SUCCESS, response));
    }

    @Override
    @PutMapping("/user/me/gender")
    public ResponseEntity<CommonResponse<UserResponse>> updateGender(AuthUser authUser, GenderUpdateRequest request) {
        UserResponse response = userFacade.updateGender(authUser.id(), request);
        return ResponseEntity
                .status(UserSuccessCode.UPDATE_GENDER_SUCCESS.getStatus())
                .body(CommonResponse.success(UserSuccessCode.UPDATE_GENDER_SUCCESS, response));
    }

    @Override
    @PutMapping("/user/profile/birth")
    public ResponseEntity<CommonResponse<UserResponse>> updateBirthYear(AuthUser authUser, BirthYearUpdateRequest request) {
        UserResponse response = userFacade.updateBirthYear(authUser.id(), request);
        return ResponseEntity
                .status(UserSuccessCode.UPDATE_BIRTH_YEAR_SUCCESS.getStatus())
                .body(CommonResponse.success(UserSuccessCode.UPDATE_BIRTH_YEAR_SUCCESS, response));
    }
}

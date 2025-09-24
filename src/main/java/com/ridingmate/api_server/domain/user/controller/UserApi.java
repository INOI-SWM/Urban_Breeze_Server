package com.ridingmate.api_server.domain.user.controller;

import com.ridingmate.api_server.domain.auth.dto.AgreementStatusResponse;
import com.ridingmate.api_server.domain.auth.dto.AgreementUpdateRequest;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.dto.UserResponse;
import com.ridingmate.api_server.domain.user.dto.request.BirthYearUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.GenderUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.IntroduceUpdateRequest;
import com.ridingmate.api_server.domain.user.dto.request.NicknameUpdateRequest;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User API", description = "사용자 관련 API")
public interface UserApi {

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공: 사용자 정보 조회 완료")
    @GetMapping("/user/profile")
    ResponseEntity<CommonResponse<UserResponse>> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser
    );

    @Operation(summary = "닉네임 변경", description = "현재 로그인된 사용자의 닉네임을 변경합니다.")
    @ApiResponse(responseCode = "200", description = "성공: 닉네임 변경 완료")
    @PutMapping("/user/profile/nickname")
    ResponseEntity<CommonResponse<UserResponse>> updateNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody NicknameUpdateRequest request
    );

    @Operation(summary = "한 줄 소개 변경", description = "현재 로그인된 사용자의 한 줄 소개를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "성공: 한 줄 소개 변경 완료")
    @PutMapping("/user/profile/introduce")
    ResponseEntity<CommonResponse<UserResponse>> updateIntroduce(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody IntroduceUpdateRequest request
    );

    @Operation(summary = "성별 변경", description = "현재 로그인된 사용자의 성별을 변경합니다.")
    @ApiResponse(responseCode = "200", description = "성공: 성별 변경 완료")
    @PutMapping("/user/me/gender")
    ResponseEntity<CommonResponse<UserResponse>> updateGender(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody GenderUpdateRequest request
    );

    @Operation(summary = "출생년도 변경", description = "현재 로그인된 사용자의 출생년도를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "성공: 출생년도 변경 완료")
    @PutMapping("/user/profile/birth")
    ResponseEntity<CommonResponse<UserResponse>> updateBirthYear(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody BirthYearUpdateRequest request
    );

    @Operation(
            summary = "프로필 이미지 변경", 
            description = "현재 로그인된 사용자의 프로필 이미지를 변경합니다. 파일을 보내지 않으면 기본 이미지로 변경됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공: 프로필 이미지 변경 완료")
    @PutMapping(value = "/user/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<CommonResponse<UserResponse>> updateProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

    @Operation(
            summary = "동의항목 업데이트", 
            description = "사용자의 동의항목을 업데이트합니다."
    )
    @ApiResponse(responseCode = "200", description = "성공: 동의항목 업데이트 완료")
    @PutMapping("/user/agreements")
    ResponseEntity<CommonResponse<AgreementStatusResponse>> updateAgreements(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AgreementUpdateRequest request
    );

    @Operation(
            summary = "회원 탈퇴", 
            description = "현재 로그인된 사용자의 계정을 삭제합니다. 개인정보는 마스킹되고, 위치정보는 법정 기간 동안 보존됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공: 회원 탈퇴 완료")
    @DeleteMapping("/user")
    ResponseEntity<CommonResponse<Void>> deleteUser(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser
    );

}

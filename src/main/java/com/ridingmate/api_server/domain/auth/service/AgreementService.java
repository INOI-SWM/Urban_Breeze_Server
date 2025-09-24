package com.ridingmate.api_server.domain.auth.service;

import com.ridingmate.api_server.domain.auth.dto.AgreementStatusResponse;
import com.ridingmate.api_server.domain.auth.dto.AgreementUpdateRequest;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.exception.UserErrorCode;
import com.ridingmate.api_server.domain.user.exception.UserException;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 동의항목 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgreementService {

    private final UserRepository userRepository;

    /**
     * 사용자의 동의항목 상태 조회
     *
     * @param user 사용자
     * @return 동의항목 상태 응답
     */
    @Transactional(readOnly = true)
    public AgreementStatusResponse getAgreementStatus(User user) {
        return AgreementStatusResponse.from(
                user.getTermsOfServiceAgreed(),
                user.getPrivacyPolicyAgreed(),
                user.getLocationServiceAgreed()
        );
    }

    /**
     * 사용자의 동의항목 업데이트
     *
     * @param userId 사용자 ID
     * @param request 동의항목 업데이트 요청
     * @return 업데이트된 동의항목 상태 응답
     */
    @Transactional
    public AgreementStatusResponse updateAgreements(Long userId, AgreementUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 동의항목 업데이트
        user.updateTermsOfServiceAgreed(request.termsOfServiceAgreed());
        user.updatePrivacyPolicyAgreed(request.privacyPolicyAgreed());
        user.updateLocationServiceAgreed(request.locationServiceAgreed());

        log.info("사용자 {}의 동의항목이 업데이트되었습니다 - 서비스약관: {}, 개인정보: {}, 위치서비스: {}", 
                userId, request.termsOfServiceAgreed(), request.privacyPolicyAgreed(), request.locationServiceAgreed());

        return AgreementStatusResponse.from(
                user.getTermsOfServiceAgreed(),
                user.getPrivacyPolicyAgreed(),
                user.getLocationServiceAgreed()
        );
    }

    /**
     * 사용자의 필수 동의항목 완료 여부 확인
     *
     * @param userId 사용자 ID
     * @return 모든 필수 동의항목 동의 여부
     */
    @Transactional(readOnly = true)
    public boolean isAllRequiredAgreementsCompleted(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        return user.getTermsOfServiceAgreed() && 
               user.getPrivacyPolicyAgreed() && 
               user.getLocationServiceAgreed();
    }

    /**
     * 사용자의 동의항목 상태를 체크하고 필요한 동의항목이 있는지 확인
     *
     * @param userId 사용자 ID
     * @return 동의가 필요한 항목이 있으면 true, 모든 동의가 완료되었으면 false
     */
    @Transactional(readOnly = true)
    public boolean hasRequiredAgreementsPending(Long userId) {
        return !isAllRequiredAgreementsCompleted(userId);
    }
}

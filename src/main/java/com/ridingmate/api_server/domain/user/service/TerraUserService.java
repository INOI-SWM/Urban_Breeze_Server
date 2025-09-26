package com.ridingmate.api_server.domain.user.service;

import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.exception.UserErrorCode;
import com.ridingmate.api_server.domain.user.exception.UserException;
import com.ridingmate.api_server.domain.user.repository.TerraUserRepository;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.infra.terra.TerraClient;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TerraUserService {

    private final UserRepository userRepository;
    private final TerraUserRepository terraUserRepository;
    private final TerraClient terraClient;

    @Transactional
    public void createTerraUser(Long userId, UUID terraUserId, TerraProvider terraProvider){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        TerraUser terraUser = TerraUser.builder()
                .user(user)
                .terraUserId(terraUserId)
                .provider(terraProvider)
                .build();

        terraUserRepository.save(terraUser);
    }

    /**
     * 사용자 삭제 시 Terra 연동 해제
     * - TerraUser 테이블의 isActive를 false로 설정
     * - 기타 외부 서비스 연동 해제
     */
    @Transactional
    public void handleUserDeletion(User user) {
        log.info("Terra 연동 해제 시작: userId={}", user.getId());
        
        try {
            // 1. 사용자의 모든 활성 Terra 연동 조회
            List<TerraUser> activeTerraUsers = terraUserRepository.findAllByUserAndIsActiveTrue(user);
            
            // 2. Terra 연동 비활성화 처리
            deactivateTerraUsers(activeTerraUsers);
            
            log.info("Terra 연동 해제 완료: userId={}, count={}", user.getId(), activeTerraUsers.size());
        } catch (Exception e) {
            log.error("Terra 연동 해제 중 오류 발생: userId={}", user.getId(), e);
            // Terra 연동 해제 실패해도 사용자 삭제는 계속 진행
        }
    }

    /**
     * Terra 연동 비활성화 처리
     */
    private void deactivateTerraUsers(List<TerraUser> terraUsers) {
        log.info("Terra 연동 비활성화 처리 시작: count={}", terraUsers.size());
        
        for (TerraUser terraUser : terraUsers) {
            try {
                // 1. Terra 서비스와의 실제 연동 해제
                deactivateTerraConnection(terraUser);
                
                // 2. 로컬 DB에서 isActive를 false로 설정
                terraUser.setInactive();
                
                log.debug("Terra 연동 비활성화 완료: terraUserId={}, provider={}", 
                    terraUser.getTerraUserId(), terraUser.getProvider());
                    
            } catch (Exception e) {
                log.warn("Terra 연동 해제 실패: terraUserId={}, provider={}", 
                    terraUser.getTerraUserId(), terraUser.getProvider(), e);
                
                // Terra 연동 해제 실패해도 로컬 DB는 비활성화
                terraUser.setInactive();
            }
        }
        
        log.info("Terra 연동 비활성화 처리 완료: count={}", terraUsers.size());
    }

    /**
     * Terra 서비스와의 실제 연동 해제
     */
    private void deactivateTerraConnection(TerraUser terraUser) {
        log.debug("Terra 서비스 연동 해제 시작: terraUserId={}, provider={}", 
            terraUser.getTerraUserId(), terraUser.getProvider());
        
        try {
            // Terra API를 통한 실제 연동 해제
            terraClient.deauthenticateUser(terraUser.getTerraUserId());
            
            log.debug("Terra 서비스 연동 해제 완료: terraUserId={}", terraUser.getTerraUserId());
            
        } catch (Exception e) {
            log.error("Terra 서비스 연동 해제 실패: terraUserId={}", terraUser.getTerraUserId(), e);
            throw e; // 상위 메서드에서 처리하도록 예외 전파
        }
    }
}

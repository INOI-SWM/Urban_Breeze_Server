package com.ridingmate.api_server.domain.auth.security;

import com.ridingmate.api_server.domain.auth.enums.SocialProvider;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 인증 시도: {}", username);
        
        // username 형식: "provider:socialId" (예: "KAKAO:1234567890")
        String[] parts = username.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("잘못된 사용자명 형식입니다: " + username);
        }
        
        try {
            SocialProvider provider = SocialProvider.valueOf(parts[0]);
            String socialId = parts[1];
            
            User user = userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(provider, socialId)
                    .orElseThrow(() -> new UsernameNotFoundException("해당하는 소셜 ID를 가진 유저를 찾을 수 없습니다: " + username));
            
            log.debug("사용자 인증 성공: userId={}, provider={}, socialId={}", 
                    user.getId(), provider, socialId);
            
            return AuthUser.from(user);
            
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("지원하지 않는 소셜 프로바이더입니다: " + parts[0]);
        }
    }
}

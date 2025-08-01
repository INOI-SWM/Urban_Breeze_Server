package com.ridingmate.api_server.global.security.filter;

import com.ridingmate.api_server.global.security.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * HTTP 요청마다 실행되는 필터 메인 로직
     * JWT 토큰을 추출하고 검증하여 Spring Security Context에 인증 정보 설정
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 인증 성공 - 사용자: {}, 요청 URI: {}", authentication.getName(), request.getRequestURI());
                
            } catch (Exception e) {
                log.warn("JWT 토큰 처리 중 오류 발생: {}, URI: {}", e.getMessage(), request.getRequestURI());

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰 추출
     * 
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 (Bearer 프리픽스 제거된 상태), 없으면 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    /**
     * 특정 요청에 대해서는 이 필터를 적용하지 않도록 설정
     * 
     * @param request HTTP 요청 객체
     * @return true면 필터 건너뛰기, false면 필터 적용
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요하지 않은 경로들
        return path.startsWith("/api/auth/") ||           // 로그인 API
               path.startsWith("/actuator/") ||           // Spring Boot Actuator
               path.startsWith("/swagger-ui/") ||         // Swagger UI
               path.startsWith("/v3/api-docs/") ||        // OpenAPI 문서
               path.equals("/") ||                        // 루트 경로
               path.equals("/health") ||                  // 헬스체크
               path.equals("/favicon.ico");               // 파비콘
    }
} 
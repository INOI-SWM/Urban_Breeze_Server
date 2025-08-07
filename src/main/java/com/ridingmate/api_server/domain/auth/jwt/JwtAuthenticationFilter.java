package com.ridingmate.api_server.domain.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.global.exception.ErrorResponse;
import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    
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

        // shouldNotFilter 체크 - 인증이 필요하지 않은 경로는 필터 건너뛰기
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                // JWT 토큰 검증 및 인증 정보 설정
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT 인증 성공 - 사용자: {}, 요청 URI: {}", authentication.getName(), request.getRequestURI());
                }
            } else {
                // 토큰이 없는 경우
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null) {
                    log.debug("Authorization 헤더가 없음 - URI: {}", request.getRequestURI());
                    setErrorResponse(response, AuthErrorCode.EMPTY_TOKEN);
                    return;
                } else {
                    log.debug("Authorization 헤더는 있지만 유효한 토큰이 없음 - URI: {}", request.getRequestURI());
                    setErrorResponse(response, AuthErrorCode.EMPTY_TOKEN);
                    return;
                }
            }
                
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰 - URI: {}", request.getRequestURI());
            setErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
            return;
            
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰 - URI: {}", request.getRequestURI());
            setErrorResponse(response, AuthErrorCode.MALFORMED_TOKEN);
            return;
            
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰 - URI: {}", request.getRequestURI());
            setErrorResponse(response, AuthErrorCode.UNSUPPORTED_TOKEN);
            return;
            
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("잘못된 JWT 서명 - URI: {}", request.getRequestURI());
            setErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
            return;
            
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 형식이 잘못됨 - URI: {}", request.getRequestURI());
            setErrorResponse(response, AuthErrorCode.EMPTY_TOKEN);
            return;
            
        } catch (Exception e) {
            log.error("JWT 처리 중 예기치 못한 오류 발생 - URI: {}, 오류: {}", request.getRequestURI(), e.getMessage());
            setErrorResponse(response, AuthErrorCode.TOKEN_VALIDATION_ERROR);
            return;
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
     * 에러 응답을 HTTP Response로 설정
     * 
     * @param response HTTP 응답 객체
     * @param errorCode JWT 에러 코드
     */
    private void setErrorResponse(HttpServletResponse response, AuthErrorCode errorCode) {
        try {
            // 응답이 이미 전송되었는지 확인
            if (response.isCommitted()) {
                log.warn("응답이 이미 커밋되어 에러 응답을 설정할 수 없습니다.");
                return;
            }

            response.setStatus(errorCode.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // CommonResponse 형식으로 통일
            CommonResponse<ErrorResponse> commonResponse = CommonResponse.error(errorCode, null);
            objectMapper.writeValue(response.getWriter(), commonResponse);

            log.debug("JWT 에러 응답 전송 - 코드: {}, 상태: {}", 
                errorCode.getCode(), errorCode.getStatus());

        } catch (IOException e) {
            log.error("JWT 에러 응답 전송 실패 (I/O 오류): {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 에러 응답 처리 중 예외 발생: {}", e.getMessage(), e);
        }
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
        
        // 디버깅을 위한 로그
        log.debug("JWT 필터 경로 체크 - URI: {}", path);
        
        // SecurityConfig의 permitAll() 경로들과 동일하게 설정
        boolean shouldNotFilter = path.startsWith("/api/auth/") ||           // 로그인 API
               path.startsWith("/actuator/") ||           // Spring Boot Actuator
               path.startsWith("/swagger-ui/") ||         // Swagger UI
               path.startsWith("/v3/api-docs") ||         // OpenAPI 문서
               path.startsWith("/api/test/") ||           // 테스트 API
               path.equals("/") ||                        // 루트 경로
               path.equals("/health") ||                  // 헬스체크
               path.equals("/favicon.ico");               // 파비콘
        
        if (shouldNotFilter) {
            log.debug("JWT 필터 제외 - URI: {}", path);
        } else {
            log.debug("JWT 필터 적용 - URI: {}", path);
        }
        
        return shouldNotFilter;
    }
} 
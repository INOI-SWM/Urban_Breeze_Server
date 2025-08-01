package com.ridingmate.api_server.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridingmate.api_server.global.exception.ErrorResponse;
import com.ridingmate.api_server.global.security.exception.JwtErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
    
    private final ObjectMapper objectMapper;
    
    /**
     * JWT 관련 예외를 캐치하고 적절한 HTTP 응답으로 변환
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 다음 필터 (JwtAuthenticationFilter 등) 실행
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰 - URI: {}, 만료 시간: {}", 
                request.getRequestURI(), e.getClaims().getExpiration());
            setErrorResponse(response, JwtErrorCode.EXPIRED_TOKEN);
            
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰 - URI: {}, 오류: {}", 
                request.getRequestURI(), e.getMessage());
            setErrorResponse(response, JwtErrorCode.MALFORMED_TOKEN);
            
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰 - URI: {}, 오류: {}", 
                request.getRequestURI(), e.getMessage());
            setErrorResponse(response, JwtErrorCode.UNSUPPORTED_TOKEN);
            
        } catch (SecurityException e) {
            log.warn("잘못된 JWT 서명 - URI: {}, 오류: {}", 
                request.getRequestURI(), e.getMessage());
            setErrorResponse(response, JwtErrorCode.INVALID_TOKEN);
            
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 형식이 잘못됨 - URI: {}, 오류: {}", 
                request.getRequestURI(), e.getMessage());
            setErrorResponse(response, JwtErrorCode.EMPTY_TOKEN);
            
        } catch (Exception e) {
            log.error("JWT 필터에서 예기치 못한 오류 발생 - URI: {}, 오류: {}", 
                request.getRequestURI(), e.getMessage(), e);
            setErrorResponse(response, JwtErrorCode.TOKEN_VALIDATION_ERROR);
        }
    }
    
    /**
     * 에러 응답을 HTTP Response로 설정
     * 
     * @param response HTTP 응답 객체
     * @param errorCode JWT 에러 코드
     */
    /**
     * ErrorResponse를 활용한 깔끔한 에러 응답 설정
     * ErrorResponse와 ObjectMapper의 안정성에 의존하는 단순한 구조
     * 
     * @param response HTTP 응답 객체
     * @param errorCode JWT 에러 코드
     */
    private void setErrorResponse(HttpServletResponse response, JwtErrorCode errorCode) {
        try {
            // 응답이 이미 전송되었는지 확인
            if (response.isCommitted()) {
                log.warn("응답이 이미 커밋되어 에러 응답을 설정할 수 없습니다.");
                return;
            }
            
            // HTTP 상태 및 헤더 설정 - ErrorCode에서 상태코드를 가져와 활용
            response.setStatus(errorCode.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            // ErrorResponse 활용 - 프로젝트의 표준 에러 응답 형식 사용
            ErrorResponse errorResponse = ErrorResponse.of(errorCode);
            
            // ObjectMapper 활용 - Spring의 안정적인 JSON 직렬화
            objectMapper.writeValue(response.getWriter(), errorResponse);
            
            log.debug("JWT 에러 응답 전송 - 코드: {}, 상태: {}", 
                errorCode.getCode(), errorCode.getStatus());
                
        } catch (IOException e) {
            // 네트워크 I/O 오류 - 이 경우 클라이언트가 응답을 받을 수 없으므로 로그만 남김
            log.error("JWT 에러 응답 전송 실패 (I/O 오류): {}", e.getMessage());
        } catch (Exception e) {
            // 예상치 못한 오류 - 시스템 안정성을 위해 로그 남김
            log.error("JWT 에러 응답 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 요청에 대해서는 이 필터를 적용하지 않도록 설정
     * JwtAuthenticationFilter와 동일한 경로 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요하지 않은 경로들 (JwtAuthenticationFilter와 동일)
        return path.startsWith("/api/auth/") ||           // 로그인 API
               path.startsWith("/actuator/") ||           // Spring Boot Actuator
               path.startsWith("/swagger-ui/") ||         // Swagger UI
               path.startsWith("/v3/api-docs/") ||        // OpenAPI 문서
               path.equals("/") ||                        // 루트 경로
               path.equals("/health") ||                  // 헬스체크
               path.equals("/favicon.ico");               // 파비콘
    }
} 
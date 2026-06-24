package com.wookyeong.jangbu_agent.infra.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청마다 Authorization 헤더의 Bearer 토큰을 검증하고 SecurityContext 에 인증 정보를 주입한다.
 *
 * <p>토큰이 없거나 유효하지 않은 경우 예외를 던지지 않고 인증 정보 없이 필터 체인을 통과시킨다.
 * 이후 {@code SecurityConfig} 의 인가 규칙에 의해 인증 필요 엔드포인트는 401 로 차단된다.
 *
 * <p>무효 사유가 "만료"인 경우 {@link #ACCESS_TOKEN_EXPIRED_ATTR} 요청 속성에 표시해 둔다.
 * {@code RestAuthenticationEntryPoint} 가 이를 읽어 만료 전용 에러코드로 응답을 분기한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ACCESS_TOKEN_EXPIRED_ATTR = "jwt.accessTokenExpired";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (jwtTokenProvider.isExpired(token)) {
                request.setAttribute(ACCESS_TOKEN_EXPIRED_ATTR, true);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

package com.wookyeong.jangbu_agent.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 요청에 MDC 트레이싱 필드를 주입하는 서블릿 필터.
 *
 * <p>Spring Security 필터 체인(order -100) 이후에 실행되므로
 * SecurityContext 에서 인증된 userId 를 읽을 수 있다.
 *
 * <p>주입 필드:
 * <ul>
 *   <li>{@code requestId} — UUID. 동일 요청의 로그를 하나로 묶는 트레이싱 키.
 *   <li>{@code userId}    — JWT 인증 사용자의 userId. 미인증 요청은 {@code "anonymous"}.
 * </ul>
 *
 * <p>MDC 는 요청 완료 후 finally 블록에서 반드시 정리된다.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", resolveUserId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }
}

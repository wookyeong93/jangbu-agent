package com.wookyeong.jangbu_agent.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("RequestLoggingFilter 단위 테스트")
class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("미인증 요청 — MDC userId가 anonymous, requestId는 UUID 형식")
    void anonymousRequest_setsAnonymousUserId() throws Exception {
        String[] capturedRequestId = new String[1];
        String[] capturedUserId = new String[1];
        FilterChain chain = (req, res) -> {
            capturedRequestId[0] = MDC.get("requestId");
            capturedUserId[0] = MDC.get("userId");
        };

        filter.doFilter(request, response, chain);

        assertThat(capturedUserId[0]).isEqualTo("anonymous");
        assertThat(capturedRequestId[0]).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("인증된 요청 — MDC userId가 인증 주체의 이름")
    void authenticatedRequest_setsUserIdFromAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of()));
        String[] capturedUserId = new String[1];
        FilterChain chain = (req, res) -> capturedUserId[0] = MDC.get("userId");

        filter.doFilter(request, response, chain);

        assertThat(capturedUserId[0]).isEqualTo("testuser");
    }

    @Test
    @DisplayName("필터 체인 정상 종료 후 MDC가 정리된다")
    void mdcClearedAfterFilterChainCompletes() throws Exception {
        FilterChain chain = (req, res) -> { };

        filter.doFilter(request, response, chain);

        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    @DisplayName("필터 체인에서 예외가 발생해도 MDC가 정리된다")
    void mdcClearedWhenFilterChainThrows() {
        FilterChain chain = (req, res) -> {
            throw new IllegalStateException("downstream failure");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(IllegalStateException.class);

        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }
}

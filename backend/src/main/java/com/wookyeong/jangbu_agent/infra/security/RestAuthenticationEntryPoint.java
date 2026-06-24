package com.wookyeong.jangbu_agent.infra.security;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.infra.security.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 미인증 요청(토큰 없음·위조·만료) 진입점.
 *
 * <p>Spring Security가 별도 entry point 없이 기본값({@code Http403ForbiddenEntryPoint})을
 * 쓰면 인증 실패도 403으로 응답해버려서, "재인증하면 통과될 수 있다"는 401의 의미와
 * "유효한 인증으로도 거부된다"는 403의 의미가 뒤섞인다. 이 클래스로 401 + {@link ApiResponse}
 * 포맷으로 명시 응답해 의미를 분리하고, 다른 예외 처리 경로(GlobalExceptionHandler)와
 * 같은 응답 포맷을 유지한다.
 *
 * <p>{@code JwtAuthFilter} 가 남긴 {@link JwtAuthFilter#ACCESS_TOKEN_EXPIRED_ATTR} 요청 속성으로
 * "만료"와 그 외 무효 사유(위조·형식오류·토큰없음)를 구분해 응답 에러코드를 분기한다 —
 * 프론트가 만료일 때만 재발급(refresh)을 시도할 수 있도록.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        boolean expired = Boolean.TRUE.equals(request.getAttribute(JwtAuthFilter.ACCESS_TOKEN_EXPIRED_ATTR));
        ErrorCode errorCode = expired ? ErrorCode.EXPIRED_TOKEN : ErrorCode.UNAUTHORIZED;
        log.warn("인증 실패: uri={}, reason={}", request.getRequestURI(), errorCode);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode));
    }
}

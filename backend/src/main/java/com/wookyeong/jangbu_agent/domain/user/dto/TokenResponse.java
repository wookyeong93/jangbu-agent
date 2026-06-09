package com.wookyeong.jangbu_agent.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인·재발급 응답.
 *
 * <p>{@code accessToken} 만 response body 에 직렬화된다.
 * {@code refreshToken} 은 {@link JsonIgnore} 로 body 노출 차단 —
 * 컨트롤러에서 HttpOnly 쿠키로만 전달한다 (ADR-0002 참고).
 */
@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;

    @JsonIgnore
    private String refreshToken;
}

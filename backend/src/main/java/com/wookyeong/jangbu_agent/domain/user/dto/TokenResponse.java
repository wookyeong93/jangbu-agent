package com.wookyeong.jangbu_agent.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인·재발급 응답.
 *
 * <p>{@code refreshToken} 은 원본 UUID 값이다 (SHA-256 해시가 아님).
 * 클라이언트는 이 값을 저장했다가 재발급 요청 시 {@code Refresh-Token} 헤더로 전송한다.
 */
@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}

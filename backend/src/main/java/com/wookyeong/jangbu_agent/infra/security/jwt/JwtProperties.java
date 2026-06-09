package com.wookyeong.jangbu_agent.infra.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml {@code jwt.*} 설정 바인딩.
 *
 * <ul>
 *   <li>{@code secret} — HMAC-SHA-256 서명 키. 운영 환경에서는 반드시 환경변수로 주입 ({@code JWT_SECRET}).
 *       최소 32바이트(256비트) 이상이어야 한다.
 *   <li>{@code accessTokenExpiry} — Access Token 만료 시간 (ms). 기본 30분.
 *   <li>{@code refreshTokenExpiry} — Refresh Token 만료 시간 (ms). 기본 7일.
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiry;
    private long refreshTokenExpiry;
}

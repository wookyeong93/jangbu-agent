package com.wookyeong.jangbu_agent.infra.security.jwt;

import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-must-be-at-least-32-bytes-long!!");
        properties.setAccessTokenExpiry(3_600_000L);   // 1h
        properties.setRefreshTokenExpiry(604_800_000L); // 7d
        provider = new JwtTokenProvider(properties);
        provider.init();
    }

    @Test
    @DisplayName("생성한 Access Token 은 유효하다")
    void generateAccessToken_isValid() {
        String token = provider.generateAccessToken("user01", 1);
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Access Token 에서 userId 와 userNo 를 정확히 추출한다")
    void getAuthentication_extractsUserInfo() {
        String token = provider.generateAccessToken("user01", 42);

        Authentication auth = provider.getAuthentication(token);
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        assertThat(principal.getUserId()).isEqualTo("user01");
        assertThat(principal.getUserNo()).isEqualTo(42);
    }

    @Test
    @DisplayName("만료된 토큰은 유효하지 않다")
    void expiredToken_isInvalid() throws InterruptedException {
        JwtProperties shortExpiry = new JwtProperties();
        shortExpiry.setSecret("test-secret-key-must-be-at-least-32-bytes-long!!");
        shortExpiry.setAccessTokenExpiry(1L); // 1ms
        shortExpiry.setRefreshTokenExpiry(604_800_000L);
        JwtTokenProvider shortProvider = new JwtTokenProvider(shortExpiry);
        shortProvider.init();

        String token = shortProvider.generateAccessToken("user01", 1);
        Thread.sleep(10);

        assertThat(shortProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("조작된 토큰은 유효하지 않다")
    void tamperedToken_isInvalid() {
        String token = provider.generateAccessToken("user01", 1);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("SHA-256 해시는 결정론적이다 — 같은 입력이면 항상 같은 출력")
    void hashToken_isDeterministic() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        assertThat(provider.hashToken(uuid)).isEqualTo(provider.hashToken(uuid));
    }

    @Test
    @DisplayName("SHA-256 해시는 입력이 다르면 출력도 다르다")
    void hashToken_differentInputs_produceDifferentHashes() {
        String hash1 = provider.hashToken("token-a");
        String hash2 = provider.hashToken("token-b");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("SHA-256 해시는 64자 hex 문자열이다")
    void hashToken_is64HexChars() {
        String hash = provider.hashToken("some-refresh-token");
        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }
}

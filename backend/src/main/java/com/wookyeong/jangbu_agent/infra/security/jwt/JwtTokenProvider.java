package com.wookyeong.jangbu_agent.infra.security.jwt;

import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Access Token 생성·검증과 Refresh Token 해싱을 담당한다.
 *
 * <p>Access Token 클레임:
 * <ul>
 *   <li>{@code sub} — userId (로그인 아이디 문자열)
 *   <li>{@code uno} — userNo (사용자 번호, 장부 격리 키)
 * </ul>
 *
 * <p>Refresh Token: {@link UUID} (opaque token). 클라이언트에는 원본 UUID 반환,
 * DB에는 {@link #hashToken(String)} 으로 SHA-256 변환 후 저장.
 * → DB 탈취 시 해시값으로 원본 UUID 역산 불가, 재사용 불가.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_USER_NO = "uno";

    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** userNo 를 {@code uno} 클레임에 포함해 발급. 서비스에서 DB 조회 없이 user_no 사용 가능. */
    public String generateAccessToken(String userId, Integer userNo) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_USER_NO, userNo)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getAccessTokenExpiry()))
                .signWith(key)
                .compact();
    }

    /** Refresh Token 원본 UUID 생성. DB 저장 전 반드시 {@link #hashToken(String)} 할 것. */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * SHA-256 해시 변환. Refresh Token을 DB에 저장하거나 조회 키로 쓸 때 사용.
     * 결정론적(동일 입력 → 동일 출력)이므로 해시값으로 {@code WHERE} 조회 가능.
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 클레임에서 {@link UserPrincipal} 을 생성해 {@link Authentication} 으로 반환.
     * DB 조회 없이 토큰 서명 검증만으로 인증 처리한다.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();
        Integer userNo = claims.get(CLAIM_USER_NO, Integer.class);

        UserPrincipal principal = new UserPrincipal(userNo, userId);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}

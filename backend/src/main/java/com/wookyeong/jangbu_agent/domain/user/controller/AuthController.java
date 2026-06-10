package com.wookyeong.jangbu_agent.domain.user.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.user.dto.LoginRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.SignupRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.TokenResponse;
import com.wookyeong.jangbu_agent.domain.user.service.AuthService;
import com.wookyeong.jangbu_agent.infra.security.jwt.JwtProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * 인증 엔드포인트. {@code /api/auth/**} 는 SecurityConfig 에서 인증 없이 허용.
 *
 * <p>Refresh Token 전달 방식:
 * <ul>
 *   <li>로그인·재발급 응답: body 에는 {@code accessToken} 만 포함.
 *       Refresh Token 은 {@code HttpOnly; Secure; SameSite=Strict} 쿠키로만 전달.
 *   <li>재발급·로그아웃 요청: 브라우저가 쿠키를 자동 전송 → JS 에서 토큰 값에 접근 불가.
 * </ul>
 */
@Tag(name = "인증", description = "회원가입·로그인·토큰 재발급·로그아웃")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.getRefreshToken()).toString())
                .body(ApiResponse.ok(tokens));  // body 에는 accessToken 만 직렬화 (@JsonIgnore)
    }

    /** 브라우저가 쿠키를 자동 전송. {@code required=false} 로 쿠키 없을 때 400 대신 서비스에서 처리. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        TokenResponse tokens = authService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.getRefreshToken()).toString())
                .body(ApiResponse.ok(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expireRefreshCookie().toString())
                .body(ApiResponse.ok());
    }

    private ResponseCookie buildRefreshCookie(String value) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(true)           // HTTPS 전용. 로컬 HTTP 테스트 시 false 로 변경
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()))
                .build();
    }

    private ResponseCookie expireRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}

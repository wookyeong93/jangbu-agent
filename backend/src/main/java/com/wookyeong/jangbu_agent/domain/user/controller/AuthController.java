package com.wookyeong.jangbu_agent.domain.user.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.user.dto.LoginRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.SignupRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.TokenResponse;
import com.wookyeong.jangbu_agent.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 엔드포인트. {@code /api/auth/**} 는 SecurityConfig 에서 인증 없이 허용.
 *
 * <ul>
 *   <li>POST /api/auth/signup  — 회원가입
 *   <li>POST /api/auth/login   — 로그인 → Access + Refresh Token
 *   <li>POST /api/auth/reissue — Refresh Token 으로 Access Token 재발급
 *   <li>POST /api/auth/logout  — 로그아웃 (Refresh Token 무효화)
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    /** {@code Refresh-Token} 헤더에 원본 UUID 를 담아 전송한다. */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok(authService.reissue(refreshToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}

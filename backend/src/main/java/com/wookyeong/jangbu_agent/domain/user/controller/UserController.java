package com.wookyeong.jangbu_agent.domain.user.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.user.dto.UpdateProfileRequest;
import com.wookyeong.jangbu_agent.domain.user.service.UserService;
import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 프로필 엔드포인트. JWT 인증 필수.
 *
 * <p>변경 가능 항목: 이름, 비밀번호.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(principal.getUserNo(), request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}

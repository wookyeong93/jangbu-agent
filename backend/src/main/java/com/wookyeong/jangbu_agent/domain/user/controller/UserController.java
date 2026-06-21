package com.wookyeong.jangbu_agent.domain.user.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.user.dto.UpdateProfileRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.UserResponse;
import com.wookyeong.jangbu_agent.domain.user.service.UserService;
import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 프로필 엔드포인트. JWT 인증 필수.
 *
 * <p>조회: 아이디·이름. 변경 가능 항목: 이름, 비밀번호.
 */
@Tag(name = "사용자", description = "사용자 프로필 조회·수정 (이름·비밀번호)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = userService.getProfile(principal.getUserNo());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(principal.getUserNo(), request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}

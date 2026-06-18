package com.wookyeong.jangbu_agent.domain.guide.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.guide.dto.GuideResponse;
import com.wookyeong.jangbu_agent.domain.guide.service.GuideService;
import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 가이드 엔드포인트. JWT 인증 필수.
 *
 * <p>모든 요청은 {@link UserPrincipal#getUserNo()} 로 격리된다.
 */
@Tag(name = "AI 가이드", description = "매입 시점·매출 피크·마진율 분석 가이드")
@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @Operation(summary = "오늘의 가이드 조회",
            description = "당일 캐시: 오늘 생성된 가이드가 있으면 즉시 반환. 없으면 집계 후 GPT 생성 → 저장 → 반환.")
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<GuideResponse>> getDailyGuide(
            @AuthenticationPrincipal UserPrincipal principal) {
        GuideResponse response = guideService.getOrCreateDailyGuide(principal.getUserNo());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

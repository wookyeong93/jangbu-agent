package com.wookyeong.jangbu_agent.domain.dashboard.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.DashboardResponse;
import com.wookyeong.jangbu_agent.domain.dashboard.service.DashboardService;
import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 KPI·추이 엔드포인트. JWT 인증 필수.
 *
 * <p>모든 요청은 {@link UserPrincipal#getUserNo()} 로 격리된다.
 */
@Tag(name = "대시보드", description = "당월 KPI 및 월별 매입·매출·순익 추이")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Validated
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 조회", description = "당월 KPI(매입/매출/지출/순익/마진율)와 최근 N개월(당월 포함) 추이를 반환한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "6") @Min(1) @Max(24) int months) {
        DashboardResponse response = dashboardService.getDashboard(principal.getUserNo(), months);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

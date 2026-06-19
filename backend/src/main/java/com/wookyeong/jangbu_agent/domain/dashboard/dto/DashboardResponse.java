package com.wookyeong.jangbu_agent.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 대시보드 응답 — 당월 KPI + 월별 추이.
 */
@Getter
@Builder
@Schema(description = "대시보드 KPI 및 월별 추이 응답")
public class DashboardResponse {

    @Schema(description = "당월 KPI")
    private MonthlyKpiDto current;

    @Schema(description = "월별 추이 (활동이 있는 달만 포함, 연·월 오름차순)")
    private List<MonthlyKpiDto> trend;
}

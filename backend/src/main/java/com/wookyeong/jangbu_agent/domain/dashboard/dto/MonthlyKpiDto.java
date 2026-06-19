package com.wookyeong.jangbu_agent.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 대시보드 KPI 응답 단위 — 당월 KPI와 월별 추이(trend) 항목에 공통으로 쓴다.
 * 마진율은 서비스 레이어에서 계산한다 (결정론적 코드, SQL에서 계산하지 않음).
 */
@Getter
@Builder
@Schema(description = "월별 KPI (당월 KPI 및 추이 항목 공통)")
public class MonthlyKpiDto {

    @Schema(description = "연도", example = "2026")
    private int year;

    @Schema(description = "월", example = "6")
    private int month;

    @Schema(description = "총 매입 금액", example = "1200000")
    private long totalPurchase;

    @Schema(description = "총 매출 금액", example = "980000")
    private long totalSale;

    @Schema(description = "총 지출 금액", example = "150000")
    private long totalExpense;

    @Schema(description = "순익 = 매출 - 매입 - 지출", example = "-370000")
    private long netProfit;

    @Schema(description = "마진율(%) = (매출 - 매입) / 매입 × 100. 매입 0이면 0", example = "-18.3")
    private double marginRate;
}

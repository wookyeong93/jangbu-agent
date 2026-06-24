package com.wookyeong.jangbu_agent.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MyBatis — 월별(year, month 기준 GROUP BY) 매입/매출/지출/순익 집계 행.
 * 활동이 있는 달만 행이 생성된다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "월별 매입/매출/지출/순익 집계 행")
public class MonthlyTrendRow {

    @Schema(description = "연도", example = "2026")
    private int year;

    @Schema(description = "월", example = "6")
    private int month;

    @Schema(description = "해당 월 총 매입 금액", example = "1200000")
    private long totalPurchase;

    @Schema(description = "해당 월 총 매출 금액", example = "980000")
    private long totalSale;

    @Schema(description = "해당 월 총 지출 금액", example = "150000")
    private long totalExpense;

    @Schema(description = "순익 = 매출 - 매입 - 지출", example = "-370000")
    private long netProfit;
}

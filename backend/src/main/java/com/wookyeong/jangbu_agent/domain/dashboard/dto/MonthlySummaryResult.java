package com.wookyeong.jangbu_agent.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MyBatis — 당월 매입/매출/지출/순익 집계 결과.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "당월 매입/매출/지출/순익 집계 결과")
public class MonthlySummaryResult {

    @Schema(description = "당월 총 매입 금액", example = "1200000")
    private long totalPurchase;

    @Schema(description = "당월 총 매출 금액", example = "980000")
    private long totalSale;

    @Schema(description = "당월 총 지출 금액", example = "150000")
    private long totalExpense;

    @Schema(description = "순익 = 매출 - 매입 - 지출", example = "-370000")
    private long netProfit;
}

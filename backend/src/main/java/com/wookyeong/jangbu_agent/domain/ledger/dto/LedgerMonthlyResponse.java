package com.wookyeong.jangbu_agent.domain.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** 월별 장부 목록 + KPI 응답. KPI 수치는 DB 미저장, 조회 시 계산. */
@Getter
@Builder
@Schema(description = "월별 장부 목록 및 KPI 응답")
public class LedgerMonthlyResponse {

    @Schema(description = "조회 연도", example = "2025")
    private int year;

    @Schema(description = "조회 월", example = "6")
    private int month;

    @Schema(description = "해당 월 장부 목록 (거래일 오름차순)")
    private List<LedgerResponse> items;

    @Schema(description = "총 매입 합계", example = "300000")
    private long totalPurchase;

    @Schema(description = "총 매출 합계", example = "500000")
    private long totalSale;

    @Schema(description = "총 지출 합계", example = "50000")
    private long totalExpense;

    @Schema(description = "순이익 = 매출 − 매입 − 지출", example = "150000")
    private long netProfit;

    @Schema(description = "마진율(%) = (매출 − 매입) / 매입 × 100. 매입 0이면 0", example = "66.67")
    private double marginRate;
}

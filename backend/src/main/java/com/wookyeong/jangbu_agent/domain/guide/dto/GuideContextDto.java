package com.wookyeong.jangbu_agent.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 가이드 생성용 집계 컨텍스트.
 * 결정론적 코드가 계산한 수치만 담는다 — LLM 은 이 DTO 를 받아 해석만 한다.
 */
@Getter
@Builder
@ToString
@Schema(description = "AI 가이드 생성용 집계 컨텍스트")
public class GuideContextDto {

    // ── 기간 요약 ──────────────────────────────────────────────────────────────

    @Schema(description = "집계 기간(일). 프롬프트·Guardrail이 함께 참조하는 기준값", example = "28")
    private int analysisPeriodDays;

    @Schema(description = "기간 내 총 매입 금액", example = "1200000")
    private long totalPurchase;

    @Schema(description = "기간 내 총 매출 금액", example = "980000")
    private long totalSale;

    @Schema(description = "기간 내 총 지출 금액", example = "150000")
    private long totalExpense;

    @Schema(description = "순익 = 매출 - 매입 - 지출", example = "-370000")
    private long netProfit;

    // ── 요일별 매출 피크 (최근 8주 평균, avgSale 내림차순) ────────────────────

    @Schema(description = "요일별 평균 매출 목록 (avgSale 내림차순)")
    private List<WeekdaySalesResult> weekdaySalesTrend;

    // ── 매입 주기 분석 ────────────────────────────────────────────────────────

    @Schema(description = "평균 매입 주기 (일). 매입 이력 2건 미만이면 null", example = "7")
    private Long avgCycleDays;

    @Schema(description = "마지막 매입 일자. 매입 이력 없으면 null", example = "2026-06-15")
    private LocalDate lastPurchaseDate;

    @Schema(description = "마지막 매입 후 경과 일수. 매입 이력 없으면 null", example = "3")
    private Long daysSinceLastPurchase;

    @Schema(description = "다음 예상 매입 일자 (lastPurchaseDate + avgCycleDays). 계산 불가 시 null", example = "2026-06-22")
    private LocalDate nextExpectedDate;

    // ── 마진율 ────────────────────────────────────────────────────────────────

    @Schema(description = "마진율 = (매출 - 매입) / 매입 × 100 (%). 매입 0이면 null", example = "-18.3")
    private Double marginRate;
}

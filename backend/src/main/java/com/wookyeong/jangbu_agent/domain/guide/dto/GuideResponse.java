package com.wookyeong.jangbu_agent.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 데일리 AI 가이드 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "데일리 AI 가이드 응답")
public class GuideResponse {

    @Schema(description = "가이드 생성 일자", example = "2026-06-19")
    private LocalDate guideDt;

    @Schema(description = "AI 생성 가이드 텍스트")
    private String guideText;

    // ── 가이드 근거 스냅샷 (최근 28일) ────────────────────────────────────────

    @Schema(description = "기준 총매입", example = "1200000")
    private long basedPurchase;

    @Schema(description = "기준 총매출", example = "980000")
    private long basedSale;

    @Schema(description = "기준 총지출", example = "150000")
    private long basedExpense;

    @Schema(description = "기준 순익 = 매출 - 매입 - 지출", example = "-370000")
    private long basedProfit;

    @Schema(description = "마진율 = (매출 - 매입) / 매입 × 100 (%). 매입 0이면 null", example = "-18.3")
    private Double marginRate;

    @Schema(description = "사용 GPT 모델명", example = "gpt-4o-mini")
    private String modelName;
}

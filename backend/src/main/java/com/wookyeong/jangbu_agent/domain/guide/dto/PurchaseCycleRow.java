package com.wookyeong.jangbu_agent.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * MyBatis — 매입 주기 분석 행 단위 결과 (Query 3).
 * gapDays: 직전 매입 대비 경과 일수. 첫 번째 행은 null.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "매입 주기 분석 행 단위 결과")
public class PurchaseCycleRow {

    @Schema(description = "매입 일자", example = "2026-06-15")
    private LocalDate trxDate;

    @Schema(description = "직전 매입 일자 (첫 행은 null)", example = "2026-06-08")
    private LocalDate prevDate;

    @Schema(description = "직전 매입 대비 경과 일수 (첫 행은 null)", example = "7")
    private Integer gapDays;
}

package com.wookyeong.jangbu_agent.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MyBatis — 요일별 평균 매출 집계 결과 (Query 2).
 * dow: 0=일, 1=월, 2=화, 3=수, 4=목, 5=금, 6=토 (PostgreSQL EXTRACT DOW 기준)
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(description = "요일별 평균 매출 집계 결과")
public class WeekdaySalesResult {

    @Schema(description = "요일 (0=일, 1=월, 2=화, 3=수, 4=목, 5=금, 6=토)", example = "5")
    private int dow;

    @Schema(description = "해당 요일 평균 매출 금액", example = "85000")
    private long avgSale;
}

package com.wookyeong.jangbu_agent.domain.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 장부 수정 요청. 수정 가능 필드: trxType, trxDate, amount, trxName. */
@Getter
@NoArgsConstructor
@Schema(description = "장부 수정 요청")
public class LedgerUpdateRequest {

    @NotBlank(message = "항목은 필수입니다.")
    @Schema(description = "거래 구분 (PURCHASE: 매입 / SALE: 매출 / EXPENSE: 지출)", example = "SALE")
    private String trxType;

    @NotNull(message = "거래일은 필수입니다.")
    @Schema(description = "거래 일자", example = "2025-06-10")
    private LocalDate trxDate;

    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    @Schema(description = "거래 금액 (0 이상 정수)", example = "80000")
    private Long amount;

    @Schema(description = "거래 명 (선택)", example = "사과 판매")
    private String trxName;
}

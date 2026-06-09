package com.wookyeong.jangbu_agent.domain.ledger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 장부 수정 요청. 수정 가능 필드: trxType, trxDate, amount, trxName. */
@Getter
@NoArgsConstructor
public class LedgerUpdateRequest {

    @NotBlank(message = "항목은 필수입니다.")
    private String trxType;

    @NotNull(message = "거래일은 필수입니다.")
    private LocalDate trxDate;

    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    private Long amount;

    private String trxName;
}

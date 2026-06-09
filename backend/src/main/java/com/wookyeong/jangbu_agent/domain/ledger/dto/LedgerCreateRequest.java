package com.wookyeong.jangbu_agent.domain.ledger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 장부 등록 요청. trxDate 미전달 시 오늘 날짜로 처리. */
@Getter
@NoArgsConstructor
public class LedgerCreateRequest {

    @NotBlank(message = "항목은 필수입니다.")
    private String trxType;

    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    private Long amount;

    private LocalDate trxDate;

    private String trxName;
}

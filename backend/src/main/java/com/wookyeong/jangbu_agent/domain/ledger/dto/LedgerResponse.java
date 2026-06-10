package com.wookyeong.jangbu_agent.domain.ledger.dto;

import com.wookyeong.jangbu_agent.domain.ledger.entity.Ledger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 단건 장부 응답. */
@Getter
@Builder
@Schema(description = "장부 단건 응답")
public class LedgerResponse {

    @Schema(description = "장부 번호", example = "1")
    private Long ledgerNo;

    @Schema(description = "거래 구분 (PURCHASE: 매입 / SALE: 매출 / EXPENSE: 지출)", example = "PURCHASE")
    private String trxType;

    @Schema(description = "거래 일자", example = "2025-06-10")
    private LocalDate trxDate;

    @Schema(description = "거래 명", example = "사과 매입")
    private String trxName;

    @Schema(description = "거래 금액", example = "50000")
    private Long amount;

    @Schema(description = "생성 일시", example = "2025-06-10T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-06-10T10:00:00")
    private LocalDateTime updatedAt;

    public static LedgerResponse from(Ledger ledger) {
        return LedgerResponse.builder()
                .ledgerNo(ledger.getLedgerNo())
                .trxType(ledger.getTrxType())
                .trxDate(ledger.getTrxDate())
                .trxName(ledger.getTrxName())
                .amount(ledger.getAmount())
                .createdAt(ledger.getCreatedAt())
                .updatedAt(ledger.getUpdatedAt())
                .build();
    }
}

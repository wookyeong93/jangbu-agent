package com.wookyeong.jangbu_agent.domain.ledger.dto;

import com.wookyeong.jangbu_agent.domain.ledger.entity.Ledger;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 단건 장부 응답. */
@Getter
@Builder
public class LedgerResponse {

    private Long ledgerNo;
    private String trxType;
    private LocalDate trxDate;
    private String trxName;
    private Long amount;
    private LocalDateTime createdAt;
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

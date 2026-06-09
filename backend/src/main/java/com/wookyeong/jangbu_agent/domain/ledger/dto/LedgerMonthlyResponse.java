package com.wookyeong.jangbu_agent.domain.ledger.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** 월별 장부 목록 + KPI 응답. KPI 수치는 DB 미저장, 조회 시 계산. */
@Getter
@Builder
public class LedgerMonthlyResponse {

    private int year;
    private int month;
    private List<LedgerResponse> items;
    private long totalPurchase;
    private long totalSale;
    private long totalExpense;
    private long netProfit;
    private double marginRate;
}

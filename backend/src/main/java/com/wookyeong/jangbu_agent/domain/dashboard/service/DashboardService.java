package com.wookyeong.jangbu_agent.domain.dashboard.service;

import com.wookyeong.jangbu_agent.domain.dashboard.dto.DashboardResponse;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlyKpiDto;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlySummaryResult;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlyTrendRow;
import com.wookyeong.jangbu_agent.domain.dashboard.repository.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 대시보드 KPI·추이 조회 로직.
 *
 * <p>모든 수치 계산(합계는 SQL, 마진율은 이 서비스)은 결정론적 코드가 수행한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    /** 당월 KPI + 최근 {@code months}개월(당월 포함) 추이를 반환한다. */
    public DashboardResponse getDashboard(Integer userNo, int months) {
        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate currentMonthEnd = today.withDayOfMonth(today.lengthOfMonth());

        MonthlySummaryResult currentSummary =
                dashboardMapper.getSummaryByPeriod(userNo, currentMonthStart, currentMonthEnd);
        MonthlyKpiDto current = toKpiDto(today.getYear(), today.getMonthValue(), currentSummary);

        LocalDate trendStart = currentMonthStart.minusMonths(months - 1L);
        List<MonthlyTrendRow> trendRows =
                dashboardMapper.getMonthlyTrend(userNo, trendStart, currentMonthEnd);
        List<MonthlyKpiDto> trend = trendRows.stream().map(this::toKpiDto).toList();

        return DashboardResponse.builder()
                .current(current)
                .trend(trend)
                .build();
    }

    private MonthlyKpiDto toKpiDto(int year, int month, MonthlySummaryResult r) {
        return MonthlyKpiDto.builder()
                .year(year)
                .month(month)
                .totalPurchase(r.getTotalPurchase())
                .totalSale(r.getTotalSale())
                .totalExpense(r.getTotalExpense())
                .netProfit(r.getNetProfit())
                .marginRate(calcMarginRate(r.getTotalPurchase(), r.getTotalSale()))
                .build();
    }

    private MonthlyKpiDto toKpiDto(MonthlyTrendRow row) {
        return MonthlyKpiDto.builder()
                .year(row.getYear())
                .month(row.getMonth())
                .totalPurchase(row.getTotalPurchase())
                .totalSale(row.getTotalSale())
                .totalExpense(row.getTotalExpense())
                .netProfit(row.getNetProfit())
                .marginRate(calcMarginRate(row.getTotalPurchase(), row.getTotalSale()))
                .build();
    }

    /** 마진율(%) = (매출 - 매입) / 매입 × 100. 매입 0이면 0 (zero-division 방지). */
    double calcMarginRate(long totalPurchase, long totalSale) {
        if (totalPurchase == 0) {
            return 0.0;
        }
        return Math.round((double) (totalSale - totalPurchase) / totalPurchase * 10000.0) / 100.0;
    }
}

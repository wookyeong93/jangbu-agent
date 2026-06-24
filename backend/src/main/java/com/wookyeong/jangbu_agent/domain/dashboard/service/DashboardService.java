package com.wookyeong.jangbu_agent.domain.dashboard.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.DashboardResponse;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlyKpiDto;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlySummaryResult;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlyTrendRow;
import com.wookyeong.jangbu_agent.domain.dashboard.repository.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
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

    /** 추이 조회 개월 수 상한. 그 이상은 차트로 보기엔 데이터가 너무 많아 비즈니스 규칙으로 막는다. */
    private static final int MAX_TREND_MONTHS = 12;

    private final DashboardMapper dashboardMapper;

    /**
     * KPI(current)는 {@code month} 기준(안 주면 오늘이 속한 달). trend는 그 달부터 과거
     * {@code months}개월(해당 달 포함) 추이 — months는 1~{@value #MAX_TREND_MONTHS}.
     */
    public DashboardResponse getDashboard(Integer userNo, YearMonth month, int months) {
        validateMonths(months);

        YearMonth targetMonth = month != null ? month : YearMonth.from(LocalDate.now());
        LocalDate targetMonthStart = targetMonth.atDay(1);
        LocalDate targetMonthEnd = targetMonth.atEndOfMonth();

        MonthlySummaryResult currentSummary =
                dashboardMapper.getSummaryByPeriod(userNo, targetMonthStart, targetMonthEnd);
        MonthlyKpiDto current = toKpiDto(targetMonth.getYear(), targetMonth.getMonthValue(), currentSummary);

        LocalDate trendStart = targetMonthStart.minusMonths(months - 1L);
        List<MonthlyTrendRow> trendRows =
                dashboardMapper.getMonthlyTrend(userNo, trendStart, targetMonthEnd);
        List<MonthlyKpiDto> trend = trendRows.stream().map(this::toKpiDto).toList();

        return DashboardResponse.builder()
                .current(current)
                .trend(trend)
                .build();
    }

    private void validateMonths(int months) {
        if (months > MAX_TREND_MONTHS) {
            throw new BusinessException(ErrorCode.DASHBOARD_PERIOD_TOO_LONG);
        }
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

package com.wookyeong.jangbu_agent.domain.dashboard.service;

import com.wookyeong.jangbu_agent.domain.dashboard.dto.DashboardResponse;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlySummaryResult;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlyTrendRow;
import com.wookyeong.jangbu_agent.domain.dashboard.repository.DashboardMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 단위 테스트")
class DashboardServiceTest {

    @Mock DashboardMapper dashboardMapper;

    @InjectMocks DashboardService dashboardService;

    // ── calcMarginRate ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("매입 0 — 마진율 0 (zero-division 방지)")
    void calcMarginRate_zeroPurchase_returnsZero() {
        assertThat(dashboardService.calcMarginRate(0L, 500_000L)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("매출 > 매입 — 마진율 양수, 소수점 2자리 반올림")
    void calcMarginRate_profit_calculatedCorrectly() {
        // (1,500,000 - 800,000) / 800,000 * 100 = 87.5
        assertThat(dashboardService.calcMarginRate(800_000L, 1_500_000L)).isEqualTo(87.5);
    }

    @Test
    @DisplayName("매출 < 매입 — 마진율 음수")
    void calcMarginRate_loss_returnsNegative() {
        // (980,000 - 1,200,000) / 1,200,000 * 100 = -18.3333... → -18.33
        assertThat(dashboardService.calcMarginRate(1_200_000L, 980_000L)).isEqualTo(-18.33);
    }

    // ── getDashboard ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("당월 KPI와 추이를 함께 반환한다")
    void getDashboard_returnsCurrentAndTrend() {
        Integer userNo = 1;
        LocalDate today = LocalDate.now();

        MonthlySummaryResult currentSummary = new MonthlySummaryResult();
        currentSummary.setTotalPurchase(1_200_000L);
        currentSummary.setTotalSale(980_000L);
        currentSummary.setTotalExpense(150_000L);
        currentSummary.setNetProfit(-370_000L);
        when(dashboardMapper.getSummaryByPeriod(eq(userNo), any(), any())).thenReturn(currentSummary);

        MonthlyTrendRow row = new MonthlyTrendRow();
        row.setYear(today.getYear());
        row.setMonth(today.getMonthValue());
        row.setTotalPurchase(1_200_000L);
        row.setTotalSale(980_000L);
        row.setTotalExpense(150_000L);
        row.setNetProfit(-370_000L);
        when(dashboardMapper.getMonthlyTrend(eq(userNo), any(), any())).thenReturn(List.of(row));

        DashboardResponse response = dashboardService.getDashboard(userNo, 6);

        assertThat(response.getCurrent().getYear()).isEqualTo(today.getYear());
        assertThat(response.getCurrent().getMonth()).isEqualTo(today.getMonthValue());
        assertThat(response.getCurrent().getTotalPurchase()).isEqualTo(1_200_000L);
        assertThat(response.getCurrent().getMarginRate()).isEqualTo(-18.33);

        assertThat(response.getTrend()).hasSize(1);
        assertThat(response.getTrend().get(0).getMarginRate()).isEqualTo(-18.33);
    }

    @Test
    @DisplayName("활동 없는 달 — 당월 KPI는 전부 0")
    void getDashboard_noActivity_currentIsAllZero() {
        Integer userNo = 1;
        when(dashboardMapper.getSummaryByPeriod(eq(userNo), any(), any()))
                .thenReturn(new MonthlySummaryResult());
        when(dashboardMapper.getMonthlyTrend(eq(userNo), any(), any())).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(userNo, 6);

        assertThat(response.getCurrent().getTotalPurchase()).isZero();
        assertThat(response.getCurrent().getMarginRate()).isEqualTo(0.0);
        assertThat(response.getTrend()).isEmpty();
    }
}

package com.wookyeong.jangbu_agent.domain.dashboard.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
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
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("month 생략 — 오늘이 속한 달 KPI와 추이를 함께 반환한다")
    void getDashboard_noMonth_usesToday() {
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

        DashboardResponse response = dashboardService.getDashboard(userNo, null, 6);

        assertThat(response.getCurrent().getYear()).isEqualTo(today.getYear());
        assertThat(response.getCurrent().getMonth()).isEqualTo(today.getMonthValue());
        assertThat(response.getCurrent().getTotalPurchase()).isEqualTo(1_200_000L);
        assertThat(response.getCurrent().getMarginRate()).isEqualTo(-18.33);

        assertThat(response.getTrend()).hasSize(1);
        assertThat(response.getTrend().get(0).getMarginRate()).isEqualTo(-18.33);
    }

    @Test
    @DisplayName("month 지정 — KPI가 오늘이 아니라 지정한 달 기준으로 조회된다")
    void getDashboard_withMonth_usesGivenMonth() {
        Integer userNo = 1;
        YearMonth pastMonth = YearMonth.of(2025, 3);

        when(dashboardMapper.getSummaryByPeriod(eq(userNo), eq(pastMonth.atDay(1)), eq(pastMonth.atEndOfMonth())))
                .thenReturn(new MonthlySummaryResult());
        when(dashboardMapper.getMonthlyTrend(eq(userNo), any(), any())).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(userNo, pastMonth, 6);

        assertThat(response.getCurrent().getYear()).isEqualTo(2025);
        assertThat(response.getCurrent().getMonth()).isEqualTo(3);
    }

    @Test
    @DisplayName("활동 없는 달 — 당월 KPI는 전부 0")
    void getDashboard_noActivity_currentIsAllZero() {
        Integer userNo = 1;
        when(dashboardMapper.getSummaryByPeriod(eq(userNo), any(), any()))
                .thenReturn(new MonthlySummaryResult());
        when(dashboardMapper.getMonthlyTrend(eq(userNo), any(), any())).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(userNo, null, 6);

        assertThat(response.getCurrent().getTotalPurchase()).isZero();
        assertThat(response.getCurrent().getMarginRate()).isEqualTo(0.0);
        assertThat(response.getTrend()).isEmpty();
    }

    @Test
    @DisplayName("months가 12 초과 — 비즈니스 에러(DASHBOARD_PERIOD_TOO_LONG)")
    void getDashboard_monthsExceeds12_throwsBusinessException() {
        assertThatThrownBy(() -> dashboardService.getDashboard(1, null, 13))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DASHBOARD_PERIOD_TOO_LONG);
    }

    @Test
    @DisplayName("months가 정확히 12 — 허용된다")
    void getDashboard_monthsExactly12_allowed() {
        when(dashboardMapper.getSummaryByPeriod(any(), any(), any())).thenReturn(new MonthlySummaryResult());
        when(dashboardMapper.getMonthlyTrend(any(), any(), any())).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(1, null, 12);

        assertThat(response.getCurrent()).isNotNull();
    }
}

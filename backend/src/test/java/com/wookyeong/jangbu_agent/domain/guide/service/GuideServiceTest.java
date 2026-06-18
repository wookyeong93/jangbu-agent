package com.wookyeong.jangbu_agent.domain.guide.service;

import com.wookyeong.jangbu_agent.domain.guide.dto.GuideContextDto;
import com.wookyeong.jangbu_agent.domain.guide.dto.PeriodSummaryResult;
import com.wookyeong.jangbu_agent.domain.guide.dto.PurchaseCycleRow;
import com.wookyeong.jangbu_agent.domain.guide.dto.WeekdaySalesResult;
import com.wookyeong.jangbu_agent.domain.guide.repository.GuideAnalysisMapper;
import com.wookyeong.jangbu_agent.domain.guide.repository.GuideRepository;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import com.wookyeong.jangbu_agent.infra.ai.OpenAiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuideService 단위 테스트")
class GuideServiceTest {

    @Mock GuideAnalysisMapper guideAnalysisMapper;
    @Mock GuideRepository guideRepository;
    @Mock UserRepository userRepository;
    @Mock OpenAiClient openAiClient;

    @InjectMocks GuideService guideService;

    // ── 목업 기준일 ─────────────────────────────────────────────────────────────

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 18);

    // ── computeAvgCycleDays ──────────────────────────────────────────────────────

    @Test
    @DisplayName("매입 이력 0건 — avgCycleDays null")
    void computeAvgCycleDays_empty_returnsNull() {
        assertThat(guideService.computeAvgCycleDays(List.of())).isNull();
    }

    @Test
    @DisplayName("매입 이력 1건 — gapDays 없으므로 avgCycleDays null")
    void computeAvgCycleDays_singleRow_returnsNull() {
        List<PurchaseCycleRow> rows = List.of(
                cycleRow(LocalDate.of(2026, 6, 15), null, null)
        );
        assertThat(guideService.computeAvgCycleDays(rows)).isNull();
    }

    @Test
    @DisplayName("매입 이력 복수건 — gapDays 평균 반올림")
    void computeAvgCycleDays_multipleRows_returnsRoundedAverage() {
        // gaps: 7, 7, 8 → 평균 7.33 → 반올림 7
        List<PurchaseCycleRow> rows = List.of(
                cycleRow(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 8),  7),
                cycleRow(LocalDate.of(2026, 6, 8),  LocalDate.of(2026, 6, 1),  7),
                cycleRow(LocalDate.of(2026, 6, 1),  LocalDate.of(2026, 5, 24), 8),
                cycleRow(LocalDate.of(2026, 5, 24), null,                      null)
        );
        assertThat(guideService.computeAvgCycleDays(rows)).isEqualTo(7L);
    }

    @Test
    @DisplayName("매입 이력 복수건 — 반올림 올림 케이스 (평균 7.5 → 8)")
    void computeAvgCycleDays_roundsUp() {
        // gaps: 7, 8 → 평균 7.5 → 반올림 8
        List<PurchaseCycleRow> rows = List.of(
                cycleRow(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 8), 7),
                cycleRow(LocalDate.of(2026, 6, 8),  LocalDate.of(2026, 5, 31), 8),
                cycleRow(LocalDate.of(2026, 5, 31), null,                      null)
        );
        assertThat(guideService.computeAvgCycleDays(rows)).isEqualTo(8L);
    }

    // ── buildContext (목업 데이터 전체 흐름) ────────────────────────────────────

    @Test
    @DisplayName("목업 데이터 — 정상 가이드 컨텍스트 조합")
    void buildContext_normalMockData_printsResult() {
        /*
         * 시나리오: 주말 매출이 높은 소규모 청과물 가게
         * 최근 4주: 매입 1,200,000 / 매출 980,000 / 지출 150,000 / 순익 -370,000
         * 피크 요일: 금(85,000) > 토(78,000) > 목(60,000)
         * 매입 주기: 7·7·8일 → 평균 7일, 마지막 매입 2026-06-15, 경과 3일
         * 예상 다음 매입: 2026-06-22
         */
        PeriodSummaryResult summary = periodSummary(1_200_000L, 980_000L, 150_000L);

        List<WeekdaySalesResult> weekday = List.of(
                weekdayRow(5, 85_000L),  // 금
                weekdayRow(6, 78_000L),  // 토
                weekdayRow(4, 60_000L),  // 목
                weekdayRow(1, 45_000L),  // 월
                weekdayRow(2, 42_000L),  // 화
                weekdayRow(3, 40_000L),  // 수
                weekdayRow(0, 30_000L)   // 일
        );

        List<PurchaseCycleRow> cycleRows = List.of(
                cycleRow(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 8),  7),
                cycleRow(LocalDate.of(2026, 6, 8),  LocalDate.of(2026, 6, 1),  7),
                cycleRow(LocalDate.of(2026, 6, 1),  LocalDate.of(2026, 5, 24), 8),
                cycleRow(LocalDate.of(2026, 5, 24), null,                      null)
        );

        GuideContextDto result = guideService.buildContext(summary, weekday, cycleRows, TODAY);

        System.out.println("\n===== 목업 GuideContextDto =====");
        System.out.println(result);
        System.out.println("================================\n");

        assertThat(result.getTotalPurchase()).isEqualTo(1_200_000L);
        assertThat(result.getTotalSale()).isEqualTo(980_000L);
        assertThat(result.getTotalExpense()).isEqualTo(150_000L);
        assertThat(result.getNetProfit()).isEqualTo(-370_000L);
        assertThat(result.getWeekdaySalesTrend()).hasSize(7);
        assertThat(result.getWeekdaySalesTrend().get(0).getDow()).isEqualTo(5);   // 금요일 1위
        assertThat(result.getAvgCycleDays()).isEqualTo(7L);
        assertThat(result.getLastPurchaseDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(result.getDaysSinceLastPurchase()).isEqualTo(3L);
        assertThat(result.getNextExpectedDate()).isEqualTo(LocalDate.of(2026, 6, 22));
    }

    @Test
    @DisplayName("목업 데이터 — 마진율 소수점 1자리 반올림")
    void buildContext_marginRate_calculatedCorrectly() {
        // (980,000 - 1,200,000) / 1,200,000 * 100 = -18.333... → -18.3
        PeriodSummaryResult summary = periodSummary(1_200_000L, 980_000L, 150_000L);
        GuideContextDto result = guideService.buildContext(summary, List.of(), List.of(), TODAY);
        assertThat(result.getMarginRate()).isEqualTo(-18.3);
    }

    @Test
    @DisplayName("매입 0 — 마진율 null")
    void buildContext_zeroPurchase_marginRateIsNull() {
        PeriodSummaryResult summary = periodSummary(0L, 500_000L, 50_000L);
        GuideContextDto result = guideService.buildContext(summary, List.of(), List.of(), TODAY);
        assertThat(result.getMarginRate()).isNull();
    }

    @Test
    @DisplayName("매입 이력 없음 — 주기 관련 필드 전부 null")
    void buildContext_noPurchaseHistory_cycleFieldsAreNull() {
        PeriodSummaryResult summary = periodSummary(0L, 500_000L, 50_000L);

        GuideContextDto result = guideService.buildContext(summary, List.of(), List.of(), TODAY);

        assertThat(result.getAvgCycleDays()).isNull();
        assertThat(result.getLastPurchaseDate()).isNull();
        assertThat(result.getDaysSinceLastPurchase()).isNull();
        assertThat(result.getNextExpectedDate()).isNull();
    }

    @Test
    @DisplayName("매입 이력 1건 — lastPurchaseDate 있지만 avgCycleDays/nextExpectedDate null")
    void buildContext_singlePurchase_noAvgNorNextDate() {
        PeriodSummaryResult summary = periodSummary(300_000L, 200_000L, 30_000L);
        List<PurchaseCycleRow> cycleRows = List.of(
                cycleRow(LocalDate.of(2026, 6, 10), null, null)
        );

        GuideContextDto result = guideService.buildContext(summary, List.of(), cycleRows, TODAY);

        assertThat(result.getLastPurchaseDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(result.getDaysSinceLastPurchase()).isEqualTo(8L);
        assertThat(result.getAvgCycleDays()).isNull();
        assertThat(result.getNextExpectedDate()).isNull();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────────

    private PeriodSummaryResult periodSummary(long purchase, long sale, long expense) {
        PeriodSummaryResult r = new PeriodSummaryResult();
        r.setTotalPurchase(purchase);
        r.setTotalSale(sale);
        r.setTotalExpense(expense);
        r.setNetProfit(sale - purchase - expense);
        return r;
    }

    private WeekdaySalesResult weekdayRow(int dow, long avgSale) {
        WeekdaySalesResult r = new WeekdaySalesResult();
        r.setDow(dow);
        r.setAvgSale(avgSale);
        return r;
    }

    private PurchaseCycleRow cycleRow(LocalDate trxDate, LocalDate prevDate, Integer gapDays) {
        PurchaseCycleRow r = new PurchaseCycleRow();
        r.setTrxDate(trxDate);
        r.setPrevDate(prevDate);
        r.setGapDays(gapDays);
        return r;
    }
}

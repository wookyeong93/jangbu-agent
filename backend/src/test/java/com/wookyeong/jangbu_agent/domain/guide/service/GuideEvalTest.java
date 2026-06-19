package com.wookyeong.jangbu_agent.domain.guide.service;

import com.wookyeong.jangbu_agent.domain.guide.dto.GuideContextDto;
import com.wookyeong.jangbu_agent.domain.guide.dto.WeekdaySalesResult;
import com.wookyeong.jangbu_agent.infra.ai.GeminiClient;
import com.wookyeong.jangbu_agent.infra.ai.GeminiProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 Gemini API를 호출해 응답 품질을 점검하는 Eval 하니스.
 *
 * <p>네트워크 호출·토큰 비용·비결정적 응답 때문에 기본 {@code test} 태스크에서 제외되며,
 * {@code ./gradlew evalTest}로만 실행된다. {@code GEMINI_API_KEY}가 없으면 자동 스킵된다.
 */
@Tag("eval")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
@DisplayName("GeminiClient Eval — 실제 호출 품질 점검")
class GuideEvalTest {

    private static GeminiClient client;
    private final GuideGuardrail guardrail = new GuideGuardrail();

    private static final LocalDate TODAY = LocalDate.now();
    private static final int ANALYSIS_PERIOD_DAYS = 28;

    @BeforeAll
    static void setUp() {
        GeminiProperties props = new GeminiProperties();
        props.setApiKey(System.getenv("GEMINI_API_KEY"));
        props.setModel("gemini-2.5-flash");
        props.setBaseUrl("https://generativelanguage.googleapis.com/v1beta");
        client = new GeminiClient(props);
    }

    @Test
    @DisplayName("정상 데이터 — 가이드 생성, 환각 없음")
    void normalData() {
        GuideContextDto ctx = GuideContextDto.builder()
                .analysisPeriodDays(ANALYSIS_PERIOD_DAYS)
                .totalPurchase(1_200_000L)
                .totalSale(980_000L)
                .totalExpense(150_000L)
                .netProfit(-370_000L)
                .marginRate(-18.3)
                .weekdaySalesTrend(List.of(weekdayRow(5, 85_000L), weekdayRow(6, 78_000L)))
                .avgCycleDays(7L)
                .lastPurchaseDate(TODAY.minusDays(3))
                .daysSinceLastPurchase(3L)
                .nextExpectedDate(TODAY.plusDays(4))
                .build();

        evaluate(ctx);
    }

    @Test
    @DisplayName("매입 없이 매출만 있음 — 가이드 생성, 환각 없음")
    void saleOnly() {
        GuideContextDto ctx = GuideContextDto.builder()
                .analysisPeriodDays(ANALYSIS_PERIOD_DAYS)
                .totalPurchase(0L)
                .totalSale(500_000L)
                .totalExpense(30_000L)
                .netProfit(470_000L)
                .build();

        evaluate(ctx);
    }

    @Test
    @DisplayName("흑자 마진 — 가이드 생성, 환각 없음")
    void profitableMargin() {
        GuideContextDto ctx = GuideContextDto.builder()
                .analysisPeriodDays(ANALYSIS_PERIOD_DAYS)
                .totalPurchase(800_000L)
                .totalSale(1_500_000L)
                .totalExpense(100_000L)
                .netProfit(600_000L)
                .marginRate(87.5)
                .avgCycleDays(10L)
                .lastPurchaseDate(TODAY.minusDays(2))
                .daysSinceLastPurchase(2L)
                .nextExpectedDate(TODAY.plusDays(8))
                .build();

        evaluate(ctx);
    }

    @Test
    @DisplayName("매입 이력 1건 — 주기 계산 불가, 가이드 생성, 환각 없음")
    void singlePurchaseHistory() {
        GuideContextDto ctx = GuideContextDto.builder()
                .analysisPeriodDays(ANALYSIS_PERIOD_DAYS)
                .totalPurchase(300_000L)
                .totalSale(200_000L)
                .totalExpense(30_000L)
                .netProfit(-130_000L)
                .lastPurchaseDate(TODAY.minusDays(8))
                .daysSinceLastPurchase(8L)
                .build();

        evaluate(ctx);
    }

    private void evaluate(GuideContextDto ctx) {
        String guideText = client.generateGuide(ctx);

        System.out.println("\n===== Eval 응답 =====");
        System.out.println(guideText);
        System.out.println("======================\n");

        assertThat(guideText).isNotBlank();

        Set<String> violations = guardrail.findViolations(guideText, ctx);
        assertThat(violations)
                .withFailMessage("컨텍스트에 없는 수치 발견: %s", violations)
                .isEmpty();
    }

    private WeekdaySalesResult weekdayRow(int dow, long avgSale) {
        WeekdaySalesResult r = new WeekdaySalesResult();
        r.setDow(dow);
        r.setAvgSale(avgSale);
        return r;
    }
}

package com.wookyeong.jangbu_agent.domain.guide.service;

import com.wookyeong.jangbu_agent.domain.guide.dto.GuideContextDto;
import com.wookyeong.jangbu_agent.domain.guide.dto.WeekdaySalesResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GuideGuardrail 단위 테스트")
class GuideGuardrailTest {

    private final GuideGuardrail guardrail = new GuideGuardrail();

    @Test
    @DisplayName("컨텍스트에 있는 숫자만 인용 — 위반 없음")
    void findViolations_allNumbersInContext_returnsEmpty() {
        GuideContextDto ctx = GuideContextDto.builder()
                .totalPurchase(1_200_000L)
                .totalSale(980_000L)
                .totalExpense(150_000L)
                .netProfit(-370_000L)
                .marginRate(-18.3)
                .avgCycleDays(7L)
                .daysSinceLastPurchase(3L)
                .lastPurchaseDate(LocalDate.of(2026, 6, 15))
                .nextExpectedDate(LocalDate.of(2026, 6, 22))
                .build();

        String text = "최근 매입은 1,200,000원, 매출은 980,000원으로 마진율 -18.3%입니다. "
                + "평균 7일 주기로 매입하며 6월 15일이 마지막 매입이었습니다. 다음 매입은 6월 22일경 추천합니다.";

        assertThat(guardrail.findViolations(text, ctx)).isEmpty();
    }

    @Test
    @DisplayName("컨텍스트에 없는 숫자 인용 — 위반 검출")
    void findViolations_fabricatedNumber_returnsViolation() {
        GuideContextDto ctx = GuideContextDto.builder()
                .totalPurchase(1_200_000L)
                .totalSale(980_000L)
                .totalExpense(150_000L)
                .netProfit(-370_000L)
                .build();

        String text = "특별히 999,999원 추가 매입을 추천합니다.";

        Set<String> violations = guardrail.findViolations(text, ctx);
        assertThat(violations).contains("999,999");
    }

    @Test
    @DisplayName("1자리 숫자(목록 번호 등)는 검증 대상에서 제외")
    void findViolations_singleDigitNumbers_areIgnored() {
        GuideContextDto ctx = GuideContextDto.builder()
                .totalPurchase(1_200_000L)
                .totalSale(980_000L)
                .totalExpense(150_000L)
                .netProfit(-370_000L)
                .build();

        String text = "1) 매입 시점 조언 2) 매출 피크 활용 3) 매입 조정 의견을 안내합니다.";

        assertThat(guardrail.findViolations(text, ctx)).isEmpty();
    }

    @Test
    @DisplayName("요일별 평균 매출 수치도 허용 목록에 포함")
    void findViolations_weekdaySalesTrendNumbers_areAllowed() {
        GuideContextDto ctx = GuideContextDto.builder()
                .totalPurchase(1_200_000L)
                .totalSale(980_000L)
                .totalExpense(150_000L)
                .netProfit(-370_000L)
                .weekdaySalesTrend(List.of(weekdayRow(5, 85_000L)))
                .build();

        String text = "금요일 평균 매출 85,000원이 가장 높습니다.";

        assertThat(guardrail.findViolations(text, ctx)).isEmpty();
    }

    private WeekdaySalesResult weekdayRow(int dow, long avgSale) {
        WeekdaySalesResult r = new WeekdaySalesResult();
        r.setDow(dow);
        r.setAvgSale(avgSale);
        return r;
    }
}

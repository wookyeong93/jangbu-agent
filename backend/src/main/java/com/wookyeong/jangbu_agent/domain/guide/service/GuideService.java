package com.wookyeong.jangbu_agent.domain.guide.service;

import com.wookyeong.jangbu_agent.domain.guide.dto.*;
import com.wookyeong.jangbu_agent.domain.guide.entity.DailyGuide;
import com.wookyeong.jangbu_agent.domain.guide.repository.GuideAnalysisMapper;
import com.wookyeong.jangbu_agent.domain.guide.repository.GuideRepository;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import com.wookyeong.jangbu_agent.infra.ai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * AI 가이드 비즈니스 로직.
 *
 * <p>모든 수치 계산은 이 서비스(결정론적 코드)가 수행한다.
 * LLM 은 {@link GuideContextDto} 를 받아 해석만 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuideService {

    private static final int ANALYSIS_PERIOD_DAYS = 28;

    private final GuideAnalysisMapper guideAnalysisMapper;
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final OpenAiClient openAiClient;

    /**
     * 오늘 가이드를 반환한다.
     *
     * <p>당일 캐시: DB에 오늘 레코드가 있으면 바로 반환.
     * 없으면 집계 후 GPT 호출 → 저장 → 반환.
     */
    @Transactional
    public GuideResponse getOrCreateDailyGuide(Integer userNo) {
        LocalDate today = LocalDate.now();

        Optional<DailyGuide> cached = guideRepository.findByUserUserNoAndGuideDt(userNo, today);
        if (cached.isPresent()) {
            return toResponse(cached.get());
        }

        GuideContextDto context = analyze(userNo);
        String guideText = openAiClient.generateGuide(context);

        User userRef = userRepository.getReferenceById(userNo);
        DailyGuide guide = DailyGuide.builder()
                .user(userRef)
                .guideDt(today)
                .context(guideText)
                .basedPurchase(context.getTotalPurchase())
                .basedSale(context.getTotalSale())
                .basedExpense(context.getTotalExpense())
                .basedProfit(context.getNetProfit())
                .modelName(openAiClient.getModelName())
                .build();

        return toResponse(guideRepository.save(guide));
    }

    /** 오늘 기준 집계 데이터를 조회·계산해 {@link GuideContextDto} 로 반환한다. */
    public GuideContextDto analyze(Integer userNo) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(ANALYSIS_PERIOD_DAYS);

        PeriodSummaryResult summary =
                guideAnalysisMapper.getSummaryByPeriod(userNo, startDate, today);
        List<WeekdaySalesResult> weekdayTrend =
                guideAnalysisMapper.getWeekdaySalesTrend(userNo);
        List<PurchaseCycleRow> cycleRows =
                guideAnalysisMapper.getPurchaseCycleRows(userNo);

        return buildContext(summary, weekdayTrend, cycleRows, today);
    }

    // ── 계산 로직 (package-private — 단위 테스트 직접 호출용) ─────────────────

    GuideContextDto buildContext(PeriodSummaryResult summary,
                                 List<WeekdaySalesResult> weekdayTrend,
                                 List<PurchaseCycleRow> cycleRows,
                                 LocalDate today) {
        LocalDate lastPurchaseDate = cycleRows.isEmpty() ? null : cycleRows.get(0).getTrxDate();
        Long avgCycleDays = computeAvgCycleDays(cycleRows);
        Long daysSinceLast = lastPurchaseDate == null ? null
                : ChronoUnit.DAYS.between(lastPurchaseDate, today);
        LocalDate nextExpected = (lastPurchaseDate != null && avgCycleDays != null)
                ? lastPurchaseDate.plusDays(avgCycleDays) : null;
        Double marginRate = summary.getTotalPurchase() == 0 ? null
                : Math.round((summary.getTotalSale() - summary.getTotalPurchase())
                        * 1000.0 / summary.getTotalPurchase()) / 10.0;

        return GuideContextDto.builder()
                .totalPurchase(summary.getTotalPurchase())
                .totalSale(summary.getTotalSale())
                .totalExpense(summary.getTotalExpense())
                .netProfit(summary.getNetProfit())
                .weekdaySalesTrend(weekdayTrend)
                .avgCycleDays(avgCycleDays)
                .lastPurchaseDate(lastPurchaseDate)
                .daysSinceLastPurchase(daysSinceLast)
                .nextExpectedDate(nextExpected)
                .marginRate(marginRate)
                .build();
    }

    /** null gapDays(첫 행) 를 제외한 평균 주기를 반올림해 반환한다. 유효 데이터 없으면 null. */
    Long computeAvgCycleDays(List<PurchaseCycleRow> rows) {
        OptionalDouble avg = rows.stream()
                .map(PurchaseCycleRow::getGapDays)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble()) : null;
    }

    private GuideResponse toResponse(DailyGuide g) {
        long purchase = g.getBasedPurchase() != null ? g.getBasedPurchase() : 0L;
        long sale = g.getBasedSale() != null ? g.getBasedSale() : 0L;
        Double marginRate = purchase == 0 ? null
                : Math.round((sale - purchase) * 1000.0 / purchase) / 10.0;

        return GuideResponse.builder()
                .guideDt(g.getGuideDt())
                .guideText(g.getContext())
                .basedPurchase(purchase)
                .basedSale(sale)
                .basedExpense(g.getBasedExpense() != null ? g.getBasedExpense() : 0L)
                .basedProfit(g.getBasedProfit() != null ? g.getBasedProfit() : 0L)
                .marginRate(marginRate)
                .modelName(g.getModelName())
                .build();
    }
}

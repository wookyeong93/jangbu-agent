package com.wookyeong.jangbu_agent.domain.guide.service;

import com.wookyeong.jangbu_agent.common.event.LedgerChangedEvent;
import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.guide.dto.*;
import com.wookyeong.jangbu_agent.domain.guide.entity.DailyGuide;
import com.wookyeong.jangbu_agent.domain.guide.repository.GuideAnalysisMapper;
import com.wookyeong.jangbu_agent.domain.guide.repository.GuideRepository;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import com.wookyeong.jangbu_agent.infra.ai.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * AI 가이드 비즈니스 로직.
 *
 * <p>모든 수치 계산은 이 서비스(결정론적 코드)가 수행한다.
 * LLM 은 {@link GuideContextDto} 를 받아 해석만 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuideService {

    private static final int ANALYSIS_PERIOD_DAYS = 28;
    private static final String NO_ACTIVITY_GUIDE_TEXT =
            "최근 28일간 등록된 매입·매출 내역이 없습니다.\n매입과 매출을 먼저 등록해 주세요.";
    private static final String GUARDRAIL_VIOLATION_GUIDE_TEXT =
            "AI 해석 결과에서 확인되지 않은 수치가 발견되어 가이드를 표시할 수 없습니다.\n잠시 후 다시 시도해 주세요.";

    private final GuideAnalysisMapper guideAnalysisMapper;
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final GuideGuardrail guideGuardrail;

    /**
     * 오늘 가이드를 반환한다.
     *
     * <p>당일 캐시: DB에 오늘 레코드가 있으면 바로 반환.
     * 없으면 집계 후 Gemini 호출 → 저장 → 반환.
     *
     * <p>매입·매출 내역이 둘 다 없으면 해석할 데이터가 없으므로 Gemini를 호출하지 않고
     * 안내 문구만 반환한다. 이 경우 DB에도 저장하지 않는다 (호출/저장 비용 모두 회피).
     *
     * <p>Gemini 응답이 {@link GuideGuardrail} 검증에 실패하면(컨텍스트에 없는 수치 포함)
     * 안전 문구로 대체하고 DB에 저장하지 않는다 — 재시도는 하지 않는다.
     */
    @Transactional
    public GuideResponse getOrCreateDailyGuide(Integer userNo) {
        LocalDate today = LocalDate.now();

        Optional<DailyGuide> cached = guideRepository.findByUserUserNoAndGuideDt(userNo, today);
        if (cached.isPresent()) {
            return toResponse(cached.get());
        }

        GuideContextDto context = analyze(userNo);
        if (hasNoActivity(context)) {
            return GuideResponse.builder()
                    .guideDt(today)
                    .guideText(NO_ACTIVITY_GUIDE_TEXT)
                    .basedPurchase(0L)
                    .basedSale(0L)
                    .basedExpense(0L)
                    .basedProfit(0L)
                    .marginRate(null)
                    .modelName(null)
                    .build();
        }

        String guideText = geminiClient.generateGuide(context);

        Set<String> violations = guideGuardrail.findViolations(guideText, context);
        if (!violations.isEmpty()) {
            log.warn("Gemini 응답에서 컨텍스트에 없는 수치 발견: {}", violations);
            return GuideResponse.builder()
                    .guideDt(today)
                    .guideText(GUARDRAIL_VIOLATION_GUIDE_TEXT)
                    .basedPurchase(context.getTotalPurchase())
                    .basedSale(context.getTotalSale())
                    .basedExpense(context.getTotalExpense())
                    .basedProfit(context.getNetProfit())
                    .marginRate(context.getMarginRate())
                    .modelName(null)
                    .build();
        }

        // JWT는 유효하지만 그 사이 계정이 삭제된 경우 — getReferenceById 의 지연 로딩에 맡기면
        // 커밋 시점에 EntityNotFoundException(500)으로 떨어지므로 미리 확인해 403으로 처리 (ADR-0006).
        if (!userRepository.existsById(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "사용자 정보를 확인할 수 없습니다.");
        }

        User userRef = userRepository.getReferenceById(userNo);
        DailyGuide guide = DailyGuide.builder()
                .user(userRef)
                .guideDt(today)
                .context(guideText)
                .basedPurchase(context.getTotalPurchase())
                .basedSale(context.getTotalSale())
                .basedExpense(context.getTotalExpense())
                .basedProfit(context.getNetProfit())
                .modelName(geminiClient.getModelName())
                .build();

        return toResponse(guideRepository.save(guide));
    }

    /**
     * 장부 변경 후 오늘자 가이드를 강제로 다시 만든다.
     *
     * <p>{@link #getOrCreateDailyGuide} 와 달리 캐시가 있어도 지우고 재생성한다 —
     * 매입·매출·지출이 바뀌면 그 변경을 반영한 가이드를 보여줘야 하기 때문.
     */
    @Transactional
    public void regenerateDailyGuide(Integer userNo) {
        guideRepository.findByUserUserNoAndGuideDt(userNo, LocalDate.now())
                .ifPresent(guideRepository::delete);
        getOrCreateDailyGuide(userNo);
    }

    /**
     * 장부 변경 이벤트 구독 — 커밋된 변경에 대해서만, 요청 스레드와 분리된 별도 스레드에서 처리한다.
     * (ledger 도메인은 이 메서드를 모른다 — {@link LedgerChangedEvent} 만 발행할 뿐이다.)
     *
     * <p>AFTER_COMMIT 시점엔 원래 트랜잭션이 이미 끝나 있어 트랜잭션이 없는 상태다 — REQUIRES_NEW로
     * 새 트랜잭션을 직접 열어야 안에서 호출하는 regenerateDailyGuide/getOrCreateDailyGuide(같은
     * 클래스 내부 호출이라 자기 자신의 @Transactional은 적용 안 됨)가 거기에 합류한다.
     * (@TransactionalEventListener 는 REQUIRES_NEW/NOT_SUPPORTED 외의 전파 옵션을 금지한다.)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLedgerChanged(LedgerChangedEvent event) {
        regenerateDailyGuide(event.userNo());
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

    /** 최근 28일간 매입·매출이 모두 0이면 해석할 데이터가 없는 것으로 본다. */
    boolean hasNoActivity(GuideContextDto ctx) {
        return ctx.getTotalPurchase() == 0 && ctx.getTotalSale() == 0;
    }

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
                .analysisPeriodDays(ANALYSIS_PERIOD_DAYS)
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

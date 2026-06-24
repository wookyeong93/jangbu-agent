package com.wookyeong.jangbu_agent.domain.ledger.service;

import com.wookyeong.jangbu_agent.common.event.LedgerChangedEvent;
import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerCreateRequest;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerMonthlyResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerUpdateRequest;
import com.wookyeong.jangbu_agent.domain.ledger.entity.Ledger;
import com.wookyeong.jangbu_agent.domain.ledger.repository.LedgerRepository;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 장부(원장) CRUD 비즈니스 로직.
 *
 * <p>모든 조회·수정·삭제는 {@code userNo} 로 격리한다.
 * 타인 장부 접근 시 LEDGER_NOT_FOUND 대신 LEDGER_ACCESS_DENIED(403) 를 던진다.
 *
 * <p>생성·수정·삭제 시 {@link LedgerChangedEvent} 를 발행한다 — guide 도메인이 이를 구독해
 * 데일리 가이드를 백그라운드에서 재생성한다. ledger가 guide를 직접 호출하지 않는 건
 * "도메인 간 직접 의존 금지" 규칙(backend CLAUDE.md) 때문이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LedgerService {

    private static final Set<String> ALLOWED_TRX_TYPES = Set.of("PURCHASE", "SALE", "EXPENSE");

    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LedgerResponse create(Integer userNo, LedgerCreateRequest request) {
        validateTrxType(request.getTrxType());

        // JWT는 유효하지만 그 사이 계정이 삭제된 경우 — getReferenceById 의 지연 로딩에 맡기면
        // 커밋 시점에 EntityNotFoundException(500)으로 떨어지므로 미리 확인해 403으로 처리 (ADR-0006).
        if (!userRepository.existsById(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "사용자 정보를 확인할 수 없습니다.");
        }

        LocalDate trxDate = request.getTrxDate() != null ? request.getTrxDate() : LocalDate.now();
        validateTrxDate(trxDate);

        Ledger ledger = Ledger.builder()
                .user(userRepository.getReferenceById(userNo))
                .trxType(request.getTrxType())
                .trxDate(trxDate)
                .trxName(request.getTrxName())
                .amount(request.getAmount())
                .build();

        LedgerResponse response = LedgerResponse.from(ledgerRepository.save(ledger));
        eventPublisher.publishEvent(new LedgerChangedEvent(userNo));
        return response;
    }

    @Transactional(readOnly = true)
    public LedgerResponse findOne(Integer userNo, Long ledgerNo) {
        return LedgerResponse.from(getOwnedLedger(userNo, ledgerNo));
    }

    @Transactional(readOnly = true)
    public LedgerMonthlyResponse findMonthly(Integer userNo, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Ledger> ledgers = ledgerRepository
                .findByUserUserNoAndTrxDateBetweenOrderByTrxDateAsc(userNo, start, end);

        long totalPurchase = sumByType(ledgers, "PURCHASE");
        long totalSale     = sumByType(ledgers, "SALE");
        long totalExpense  = sumByType(ledgers, "EXPENSE");
        long netProfit     = totalSale - totalPurchase - totalExpense;
        double marginRate  = totalPurchase == 0 ? 0.0
                : Math.round((double) (totalSale - totalPurchase) / totalPurchase * 10000.0) / 100.0;

        return LedgerMonthlyResponse.builder()
                .year(year)
                .month(month)
                .items(ledgers.stream().map(LedgerResponse::from).toList())
                .totalPurchase(totalPurchase)
                .totalSale(totalSale)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .marginRate(marginRate)
                .build();
    }

    public LedgerResponse update(Integer userNo, Long ledgerNo, LedgerUpdateRequest request) {
        validateTrxType(request.getTrxType());
        validateTrxDate(request.getTrxDate());
        Ledger ledger = getOwnedLedger(userNo, ledgerNo);
        ledger.update(request.getTrxType(), request.getTrxDate(), request.getTrxName(), request.getAmount());
        eventPublisher.publishEvent(new LedgerChangedEvent(userNo));
        return LedgerResponse.from(ledger);
    }

    public void delete(Integer userNo, Long ledgerNo) {
        ledgerRepository.delete(getOwnedLedger(userNo, ledgerNo));
        eventPublisher.publishEvent(new LedgerChangedEvent(userNo));
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private void validateTrxType(String trxType) {
        if (!ALLOWED_TRX_TYPES.contains(trxType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "항목은 PURCHASE/SALE/EXPENSE 중 하나여야 합니다.");
        }
    }

    /** 장부는 이미 발생한 거래만 기록한다 — 미래 날짜는 거부한다 (ADR-0007). 과거 날짜는 허용. */
    private void validateTrxDate(LocalDate trxDate) {
        if (trxDate.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.LEDGER_FUTURE_DATE_NOT_ALLOWED);
        }
    }

    private Ledger getOwnedLedger(Integer userNo, Long ledgerNo) {
        Ledger ledger = ledgerRepository.findById(ledgerNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEDGER_NOT_FOUND));
        if (!ledger.getUser().getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.LEDGER_ACCESS_DENIED);
        }
        return ledger;
    }

    private long sumByType(List<Ledger> ledgers, String type) {
        return ledgers.stream()
                .filter(l -> type.equals(l.getTrxType()))
                .mapToLong(Ledger::getAmount)
                .sum();
    }
}

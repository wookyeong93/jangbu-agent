package com.wookyeong.jangbu_agent.domain.ledger.service;

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
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LedgerService {

    private static final Set<String> ALLOWED_TRX_TYPES = Set.of("PURCHASE", "SALE", "EXPENSE");

    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    public LedgerResponse create(Integer userNo, LedgerCreateRequest request) {
        validateTrxType(request.getTrxType());

        LocalDate trxDate = request.getTrxDate() != null ? request.getTrxDate() : LocalDate.now();

        Ledger ledger = Ledger.builder()
                .user(userRepository.getReferenceById(userNo))
                .trxType(request.getTrxType())
                .trxDate(trxDate)
                .trxName(request.getTrxName())
                .amount(request.getAmount())
                .build();

        return LedgerResponse.from(ledgerRepository.save(ledger));
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
        Ledger ledger = getOwnedLedger(userNo, ledgerNo);
        ledger.update(request.getTrxType(), request.getTrxDate(), request.getTrxName(), request.getAmount());
        return LedgerResponse.from(ledger);
    }

    public void delete(Integer userNo, Long ledgerNo) {
        ledgerRepository.delete(getOwnedLedger(userNo, ledgerNo));
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private void validateTrxType(String trxType) {
        if (!ALLOWED_TRX_TYPES.contains(trxType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "항목은 PURCHASE/SALE/EXPENSE 중 하나여야 합니다.");
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

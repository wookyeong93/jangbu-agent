package com.wookyeong.jangbu_agent.domain.ledger.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerCreateRequest;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerMonthlyResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerUpdateRequest;
import com.wookyeong.jangbu_agent.domain.ledger.entity.Ledger;
import com.wookyeong.jangbu_agent.domain.ledger.repository.LedgerRepository;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerService 단위 테스트")
class LedgerServiceTest {

    @Mock LedgerRepository ledgerRepository;
    @Mock UserRepository userRepository;

    @InjectMocks LedgerService ledgerService;

    private static final Integer USER_NO = 1;
    private static final Integer OTHER_USER_NO = 2;

    // ── 등록 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("등록 성공 — trxDate 미전달 시 오늘 날짜 저장")
    void create_noTrxDate_usesToday() {
        User user = User.builder().userNo(USER_NO).userId("u1").userPwd("pw").build();
        Ledger saved = buildLedger(10L, user, "PURCHASE", LocalDate.now(), null, 5000L);

        given(userRepository.getReferenceById(USER_NO)).willReturn(user);
        given(ledgerRepository.save(any())).willReturn(saved);

        LedgerCreateRequest req = makeCreateRequest("PURCHASE", 5000L, null, null);
        LedgerResponse result = ledgerService.create(USER_NO, req);

        assertThat(result.getTrxType()).isEqualTo("PURCHASE");
        assertThat(result.getAmount()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("등록 실패 — 허용되지 않은 trxType")
    void create_invalidTrxType_throwsException() {
        LedgerCreateRequest req = makeCreateRequest("REFUND", 1000L, null, null);

        assertThatThrownBy(() -> ledgerService.create(USER_NO, req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    // ── 단건 조회 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 성공")
    void findOne_success() {
        User user = User.builder().userNo(USER_NO).userId("u1").userPwd("pw").build();
        Ledger ledger = buildLedger(1L, user, "SALE", LocalDate.now(), "사과", 20000L);
        given(ledgerRepository.findById(1L)).willReturn(Optional.of(ledger));

        LedgerResponse result = ledgerService.findOne(USER_NO, 1L);

        assertThat(result.getLedgerNo()).isEqualTo(1L);
        assertThat(result.getTrxType()).isEqualTo("SALE");
    }

    @Test
    @DisplayName("단건 조회 실패 — 존재하지 않으면 LEDGER_NOT_FOUND")
    void findOne_notFound_throwsException() {
        given(ledgerRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.findOne(USER_NO, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LEDGER_NOT_FOUND);
    }

    @Test
    @DisplayName("단건 조회 실패 — 타인 장부 접근 시 LEDGER_ACCESS_DENIED")
    void findOne_accessDenied_throwsException() {
        User owner = User.builder().userNo(OTHER_USER_NO).userId("u2").userPwd("pw").build();
        Ledger ledger = buildLedger(1L, owner, "PURCHASE", LocalDate.now(), null, 1000L);
        given(ledgerRepository.findById(1L)).willReturn(Optional.of(ledger));

        assertThatThrownBy(() -> ledgerService.findOne(USER_NO, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LEDGER_ACCESS_DENIED);
    }

    // ── 월별 조회 + KPI ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("월별 조회 — KPI 정상 계산")
    void findMonthly_kpiCalculation() {
        User user = User.builder().userNo(USER_NO).userId("u1").userPwd("pw").build();
        List<Ledger> ledgers = List.of(
                buildLedger(1L, user, "PURCHASE", LocalDate.of(2025, 6, 1),  null, 10000L),
                buildLedger(2L, user, "SALE",     LocalDate.of(2025, 6, 5),  null, 25000L),
                buildLedger(3L, user, "EXPENSE",  LocalDate.of(2025, 6, 10), null, 3000L)
        );
        given(ledgerRepository.findByUserUserNoAndTrxDateBetweenOrderByTrxDateAsc(
                USER_NO, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30)))
                .willReturn(ledgers);

        LedgerMonthlyResponse result = ledgerService.findMonthly(USER_NO, 2025, 6);

        assertThat(result.getTotalPurchase()).isEqualTo(10000L);
        assertThat(result.getTotalSale()).isEqualTo(25000L);
        assertThat(result.getTotalExpense()).isEqualTo(3000L);
        assertThat(result.getNetProfit()).isEqualTo(12000L);    // 25000 - 10000 - 3000
        assertThat(result.getMarginRate()).isEqualTo(150.0);    // (25000-10000)/10000 * 100
        assertThat(result.getItems()).hasSize(3);
    }

    @Test
    @DisplayName("월별 조회 — 기록 없으면 모든 KPI 0")
    void findMonthly_empty_allZero() {
        given(ledgerRepository.findByUserUserNoAndTrxDateBetweenOrderByTrxDateAsc(
                USER_NO, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)))
                .willReturn(List.of());

        LedgerMonthlyResponse result = ledgerService.findMonthly(USER_NO, 2025, 1);

        assertThat(result.getTotalPurchase()).isZero();
        assertThat(result.getTotalSale()).isZero();
        assertThat(result.getTotalExpense()).isZero();
        assertThat(result.getNetProfit()).isZero();
        assertThat(result.getMarginRate()).isZero();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("월별 조회 — 매입 0 이면 마진율 0 (zero-division 방지)")
    void findMonthly_zeroPurchase_marginRateIsZero() {
        User user = User.builder().userNo(USER_NO).userId("u1").userPwd("pw").build();
        List<Ledger> ledgers = List.of(
                buildLedger(1L, user, "SALE", LocalDate.of(2025, 3, 1), null, 5000L)
        );
        given(ledgerRepository.findByUserUserNoAndTrxDateBetweenOrderByTrxDateAsc(
                USER_NO, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31)))
                .willReturn(ledgers);

        LedgerMonthlyResponse result = ledgerService.findMonthly(USER_NO, 2025, 3);

        assertThat(result.getMarginRate()).isZero();
    }

    // ── 수정 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 성공")
    void update_success() {
        User user = User.builder().userNo(USER_NO).userId("u1").userPwd("pw").build();
        Ledger ledger = buildLedger(1L, user, "매입", LocalDate.of(2025, 6, 1), null, 1000L);
        given(ledgerRepository.findById(1L)).willReturn(Optional.of(ledger));

        LedgerUpdateRequest req = makeUpdateRequest("SALE", LocalDate.of(2025, 6, 2), "수정됨", 9999L);
        LedgerResponse result = ledgerService.update(USER_NO, 1L, req);

        assertThat(result.getTrxType()).isEqualTo("SALE");
        assertThat(result.getAmount()).isEqualTo(9999L);
        assertThat(result.getTrxName()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("수정 실패 — 존재하지 않으면 LEDGER_NOT_FOUND")
    void update_notFound_throwsException() {
        given(ledgerRepository.findById(99L)).willReturn(Optional.empty());
        LedgerUpdateRequest req = makeUpdateRequest("PURCHASE", LocalDate.now(), null, 100L);

        assertThatThrownBy(() -> ledgerService.update(USER_NO, 99L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LEDGER_NOT_FOUND);
    }

    @Test
    @DisplayName("수정 실패 — 타인 장부 수정 시 LEDGER_ACCESS_DENIED")
    void update_accessDenied_throwsException() {
        User owner = User.builder().userNo(OTHER_USER_NO).userId("u2").userPwd("pw").build();
        Ledger ledger = buildLedger(1L, owner, "PURCHASE", LocalDate.now(), null, 500L);
        given(ledgerRepository.findById(1L)).willReturn(Optional.of(ledger));
        LedgerUpdateRequest req = makeUpdateRequest("PURCHASE", LocalDate.now(), null, 500L);

        assertThatThrownBy(() -> ledgerService.update(USER_NO, 1L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LEDGER_ACCESS_DENIED);
    }

    @Test
    @DisplayName("수정 실패 — 허용되지 않은 trxType")
    void update_invalidTrxType_throwsException() {
        LedgerUpdateRequest req = makeUpdateRequest("REFUND", LocalDate.now(), null, 100L);

        assertThatThrownBy(() -> ledgerService.update(USER_NO, 1L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    // ── 삭제 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("삭제 성공")
    void delete_success() {
        User user = User.builder().userNo(USER_NO).userId("u1").userPwd("pw").build();
        Ledger ledger = buildLedger(1L, user, "EXPENSE", LocalDate.now(), null, 200L);
        given(ledgerRepository.findById(1L)).willReturn(Optional.of(ledger));

        ledgerService.delete(USER_NO, 1L);

        verify(ledgerRepository).delete(ledger);
    }

    @Test
    @DisplayName("삭제 실패 — 존재하지 않으면 LEDGER_NOT_FOUND")
    void delete_notFound_throwsException() {
        given(ledgerRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.delete(USER_NO, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LEDGER_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제 실패 — 타인 장부 삭제 시 LEDGER_ACCESS_DENIED")
    void delete_accessDenied_throwsException() {
        User owner = User.builder().userNo(OTHER_USER_NO).userId("u2").userPwd("pw").build();
        Ledger ledger = buildLedger(1L, owner, "EXPENSE", LocalDate.now(), null, 300L);
        given(ledgerRepository.findById(1L)).willReturn(Optional.of(ledger));

        assertThatThrownBy(() -> ledgerService.delete(USER_NO, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LEDGER_ACCESS_DENIED);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────────

    private Ledger buildLedger(Long ledgerNo, User user, String trxType,
                                LocalDate trxDate, String trxName, Long amount) {
        return Ledger.builder()
                .ledgerNo(ledgerNo)
                .user(user)
                .trxType(trxType)
                .trxDate(trxDate)
                .trxName(trxName)
                .amount(amount)
                .build();
    }

    private LedgerCreateRequest makeCreateRequest(String trxType, Long amount,
                                                   LocalDate trxDate, String trxName) {
        LedgerCreateRequest req = new LedgerCreateRequest();
        setField(req, "trxType", trxType);
        setField(req, "amount", amount);
        setField(req, "trxDate", trxDate);
        setField(req, "trxName", trxName);
        return req;
    }

    private LedgerUpdateRequest makeUpdateRequest(String trxType, LocalDate trxDate,
                                                   String trxName, Long amount) {
        LedgerUpdateRequest req = new LedgerUpdateRequest();
        setField(req, "trxType", trxType);
        setField(req, "trxDate", trxDate);
        setField(req, "trxName", trxName);
        setField(req, "amount", amount);
        return req;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.wookyeong.jangbu_agent.domain.ledger.repository;

import com.wookyeong.jangbu_agent.domain.ledger.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 원장 데이터 접근.
 *
 * <p>월별 목록 조회는 {@code trxDate} 범위로 필터링하며, 반드시 {@code userNo} 로 격리할 것.
 */
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findByUserUserNoAndTrxDateBetweenOrderByTrxDateAsc(
            Integer userNo, LocalDate start, LocalDate end);
}

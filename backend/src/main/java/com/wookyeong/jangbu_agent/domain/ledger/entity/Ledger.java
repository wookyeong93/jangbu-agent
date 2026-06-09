package com.wookyeong.jangbu_agent.domain.ledger.entity;

import com.wookyeong.jangbu_agent.common.entity.BaseEntity;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 원장 엔티티 (tb_ledger).
 *
 * <p>매입 · 매출 · 지출을 단일 테이블로 관리한다.
 * 구분은 {@code trxType} 으로 한다 (tb_code 의 그룹코드 "TRX_TYPE" 하위 값).
 *
 * <p>도메인 규칙:
 * <ul>
 *   <li>{@code amount} 는 양수 절댓값만 저장한다. 부호 연산 금지.
 *   <li>순익 = 매출합 − 매입합 − 지출합 (집계는 MyBatis 쿼리로 처리).
 *   <li>조회는 반드시 {@code user.userNo} 로 격리할 것.
 * </ul>
 */
@Entity
@Table(name = "tb_ledger", indexes = {
        @Index(name = "idx_ledger_01", columnList = "user_no, trx_date"),
        @Index(name = "idx_ledger_02", columnList = "user_no, trx_type, trx_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Ledger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_no")
    private Long ledgerNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private User user;

    /** tb_code 그룹코드 "TRX_TYPE" 의 code 값. 예: "BUY"(매입), "SALE"(매출), "EXPENSE"(지출). */
    @Column(name = "trx_type", nullable = false, length = 50)
    private String trxType;

    @Column(name = "trx_date", nullable = false)
    private LocalDate trxDate;

    @Column(name = "trx_name", columnDefinition = "TEXT")
    private String trxName;

    /** 양수 절댓값. 음수 저장 금지. */
    @Column(name = "amount", nullable = false)
    private Long amount;

    public void update(String trxType, LocalDate trxDate, String trxName, Long amount) {
        this.trxType = trxType;
        this.trxDate = trxDate;
        this.trxName = trxName;
        this.amount = amount;
    }
}

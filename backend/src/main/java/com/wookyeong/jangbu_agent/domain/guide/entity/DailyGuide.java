package com.wookyeong.jangbu_agent.domain.guide.entity;

import com.wookyeong.jangbu_agent.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 데일리 AI 가이드 엔티티 (tb_daily_guide).
 *
 * <p>LLM 이 가이드를 생성할 때 근거로 쓴 집계 수치를 스냅샷으로 저장한다.
 * 응답에 포함되는 모든 수치는 이 스냅샷에서만 온다 — LLM 이 숫자를 추정·생성하지 않는다.
 *
 * <p>사용자당 하루 1건만 허용 (UNIQUE: user_no + guide_dt).
 * 수정이 없으므로 updated_at/by 필드가 없다. BaseEntity 를 상속하지 않는다.
 */
@Entity
@Table(name = "tb_daily_guide", uniqueConstraints =
        @UniqueConstraint(name = "uq_daily_guide_user_dt", columnNames = {"user_no", "guide_dt"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DailyGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_no")
    private Long dailyNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private User user;

    @Column(name = "guide_dt", nullable = false)
    private LocalDate guideDt;

    /** LLM 이 생성한 가이드 텍스트. */
    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    // ── 가이드 생성 시점 집계 스냅샷 ─────────────────────────────────────────

    @Column(name = "based_purchase")
    private Long basedPurchase;

    @Column(name = "based_sale")
    private Long basedSale;

    @Column(name = "based_expense")
    private Long basedExpense;

    /** 순익 스냅샷 = based_sale − based_purchase − based_expense */
    @Column(name = "based_profit")
    private Long basedProfit;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}

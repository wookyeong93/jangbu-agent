package com.wookyeong.jangbu_agent.domain.code.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 코드 엔티티 (tb_code).
 *
 * <p>복합 PK(group_code + code)를 {@link CodeId} 로 관리한다.
 * {@code groupCodeEntity} 는 group_code 컬럼을 CodeId 가 이미 관리하므로
 * insertable/updatable = false 로 읽기 전용 매핑.
 *
 * <p>tb_ledger.trx_type 에 들어가는 값은 이 테이블의 code 컬럼 값이다.
 */
@Entity
@Table(name = "tb_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Code {

    @EmbeddedId
    private CodeId id;

    @Column(name = "code_name", nullable = false, length = 255)
    private String codeName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 'Y' / 'N'. 'N' 인 경우 화면에서 노출하지 않는다. */
    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;

    // group_code 컬럼은 CodeId 에서 이미 관리 → 읽기 전용 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code", insertable = false, updatable = false)
    private GroupCode groupCodeEntity;
}

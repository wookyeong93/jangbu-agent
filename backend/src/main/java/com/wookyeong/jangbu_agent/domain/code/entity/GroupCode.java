package com.wookyeong.jangbu_agent.domain.code.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 그룹 코드 엔티티 (tb_group_code).
 *
 * <p>코드 체계의 상위 분류. 예: "TRX_TYPE"(거래 구분), "CATEGORY" 등.
 * 하위 코드 목록은 {@link Code} 참고.
 */
@Entity
@Table(name = "tb_group_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GroupCode {

    @Id
    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 'Y' / 'N'. 'N' 인 경우 화면에서 노출하지 않는다. */
    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;
}

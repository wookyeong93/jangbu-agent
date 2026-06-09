package com.wookyeong.jangbu_agent.domain.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * tb_code 복합 PK (group_code + code).
 *
 * <p>{@code @Embeddable} PK 는 반드시 {@link Serializable} 구현 및
 * {@code equals/hashCode} 재정의가 필요하다 (JPA 스펙).
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CodeId implements Serializable {

    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(name = "code", length = 50)
    private String code;
}

package com.wookyeong.jangbu_agent.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 생성/수정 audit 필드 공통 베이스.
 *
 * <p>tb_user, tb_ledger 처럼 created_at/by, updated_at/by 를 모두 가진 테이블에서 상속.
 * 값은 JPA Auditing 이 자동으로 채운다 ({@link JpaConfig} 참고).
 * 엔티티에서 직접 설정하지 말 것.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false, length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}

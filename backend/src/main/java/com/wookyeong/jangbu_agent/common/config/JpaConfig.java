package com.wookyeong.jangbu_agent.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA Auditing 설정.
 *
 * <p>{@code auditorAware} 빈이 created_by / updated_by 에 들어갈 값을 결정한다.
 * 현재는 "system" 고정 (JWT 미연결 단계).
 *
 * <p>TODO (JWT 연결 단계): SecurityContextHolder 에서 인증 사용자 ID를 꺼내도록 교체.
 * <pre>
 * return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
 *         .map(Authentication::getName);
 * </pre>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }
}

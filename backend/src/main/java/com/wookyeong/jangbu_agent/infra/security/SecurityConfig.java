package com.wookyeong.jangbu_agent.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정.
 *
 * <p>현재 상태 (개발 1단계):
 * - 세션 비활성화 (Stateless JWT 구조)
 * - CSRF 비활성화 (REST API + JWT 조합에서 불필요)
 * - 모든 요청 허용 — JWT 필터 구현 후 아래 TODO 로 교체 예정
 *
 * <p>TODO (JWT 연결 단계):
 * <pre>
 * .authorizeHttpRequests(auth -> auth
 *     .requestMatchers("/api/auth/**").permitAll()
 *     .anyRequest().authenticated())
 * .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
 * </pre>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.wookyeong.jangbu_agent.infra.security;

import com.wookyeong.jangbu_agent.infra.security.jwt.JwtAuthFilter;
import com.wookyeong.jangbu_agent.infra.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정.
 *
 * <p>인가 규칙:
 * <ul>
 *   <li>{@code /api/auth/**} — 인증 없이 허용 (회원가입·로그인·재발급)
 *   <li>그 외 {@code /api/**} — JWT Access Token 필수
 * </ul>
 *
 * <p>세션 미사용 (Stateless), CSRF 비활성화 (REST API + JWT 조합).
 * 인증 실패(토큰 없음·위조·만료)는 {@link RestAuthenticationEntryPoint}가 401 +
 * {@code ApiResponse} 포맷으로 응답한다 — entry point를 지정하지 않으면 Spring
 * Security 기본값({@code Http403ForbiddenEntryPoint})이 403을 내려보내 "재인증하면
 * 통과될 수 있다(401)"는 의미와 "유효한 인증으로도 거부된다(403)"는 의미가 섞인다.
 *
 * <p>CORS: 프론트(Nuxt dev 서버, localhost:3000)에서 cross-origin으로 호출하고
 * refresh token을 httpOnly 쿠키로 주고받으므로 {@code allowCredentials(true)} 필수
 * (이 경우 {@code allowedOrigins}에 와일드카드 "*" 사용 불가, 출처를 명시해야 함).
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

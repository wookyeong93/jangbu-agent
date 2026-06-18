package com.wookyeong.jangbu_agent.infra.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml {@code gemini.*} 설정 바인딩.
 *
 * <ul>
 *   <li>{@code apiKey} — Gemini API 키. 운영 환경에서는 환경변수로 주입 ({@code GEMINI_API_KEY}).
 *   <li>{@code model} — 사용할 Gemini 모델명. 기본값 {@code gemini-2.5-flash} (무료 티어 대상).
 *   <li>{@code baseUrl} — Gemini API 기본 URL.
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey;
    private String model;
    private String baseUrl;
}

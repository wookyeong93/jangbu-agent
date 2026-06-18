package com.wookyeong.jangbu_agent.infra.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml {@code openai.*} 설정 바인딩.
 *
 * <ul>
 *   <li>{@code apiKey} — OpenAI API 키. 운영 환경에서는 환경변수로 주입 ({@code OPENAI_API_KEY}).
 *   <li>{@code model} — 사용할 GPT 모델명. 기본값 {@code gpt-4o-mini}.
 *   <li>{@code baseUrl} — OpenAI API 기본 URL.
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String apiKey;
    private String model;
    private String baseUrl;
}

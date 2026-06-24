package com.wookyeong.jangbu_agent.infra.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gemini 인프라 설정 — {@link GeminiProperties} 바인딩 활성화.
 */
@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {
}

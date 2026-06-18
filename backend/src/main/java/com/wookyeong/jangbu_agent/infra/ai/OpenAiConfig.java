package com.wookyeong.jangbu_agent.infra.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI 인프라 설정 — {@link OpenAiProperties} 바인딩 활성화.
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {
}

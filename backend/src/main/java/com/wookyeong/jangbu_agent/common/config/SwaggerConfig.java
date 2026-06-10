package com.wookyeong.jangbu_agent.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI 3) 설정.
 *
 * <p>모든 엔드포인트에 Bearer JWT 인증 스킴을 적용한다.
 * Swagger UI 에서 로그인 후 발급된 accessToken 을 Authorize 에 입력하면
 * 이후 요청에 자동으로 Authorization 헤더가 추가된다.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("장부 에이전트 API")
                        .description("소상공인 매입/판매 장부 + AI 매입 가이드 에이전트")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * springdoc 2.x 의 QuerydslPredicateOperationCustomizer 가
     * Spring Data 4.x 에서 제거된 TypeInformation 을 참조해 기동 실패하는 문제 우회.
     * 빈 정의를 Spring 이 초기화하기 전에 제거한다.
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor removeQuerydslOperationCustomizer() {
        return (BeanDefinitionRegistry registry) -> {
            if (registry.containsBeanDefinition("queryDslQuerydslPredicateOperationCustomizer")) {
                registry.removeBeanDefinition("queryDslQuerydslPredicateOperationCustomizer");
            }
        };
    }
}

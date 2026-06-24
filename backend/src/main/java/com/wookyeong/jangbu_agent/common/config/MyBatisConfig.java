package com.wookyeong.jangbu_agent.common.config;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 수동 설정.
 *
 * <p>mybatis-spring-boot-starter 자동 설정이 Spring Boot 4 에서 동작하지 않아
 * {@link SqlSessionFactory} / {@link SqlSessionTemplate} 을 직접 등록한다.
 * XML 매퍼: {@code classpath:mapper/**⁠/*.xml}
 * 카멜케이스 매핑: ON (DB snake_case ↔ Java camelCase 자동 변환)
 */
@Configuration
@MapperScan(basePackages = "com.wookyeong.jangbu_agent", annotationClass = Mapper.class)
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/**/*.xml"));

        org.apache.ibatis.session.Configuration config =
                new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(config);

        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

package com.wookyeong.jangbu_agent.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * {@code @Async} 활성화 설정.
 *
 * <p>현재 유일한 사용처: 장부 변경 시 데일리 가이드를 백그라운드에서 재생성하는
 * {@code GuideService.onLedgerChanged} — 요청 스레드를 Gemini 호출 시간만큼 묶어두지 않기 위함.
 * 별도 메시지 큐 없이 스레드풀만 사용 — 트래픽이 거의 없는 토이 프로젝트 규모라 작업 유실 위험을
 * 감수할 만하고(다음 거래·다음 날 재생성으로 자연 복구), 큐 운영 비용을 들일 이유가 없다.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    /** {@code @Async void} 메서드는 예외가 호출자에게 전파되지 않으므로 직접 로깅한다. */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncUncaughtExceptionHandler();
    }

    @Slf4j
    static class LoggingAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("비동기 메서드 실행 중 예외 발생: {}", method.getName(), ex);
        }
    }
}

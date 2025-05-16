package com.ddhouse.chat.config;

import com.ddhouse.chat.handler.LoggingAsyncExecutor;
import com.ddhouse.chat.handler.MyAsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    // @Async 메서드에서 발생하는 예외는 호출자에게 전파가 되지 않음 => AsyncUncaughtExceptionHandler를 사용하여 예외 처리

    @Override
    public Executor getAsyncExecutor() {
        LoggingAsyncExecutor executor = new LoggingAsyncExecutor();
        executor.setCorePoolSize(2);        // 기본 쓰레드 수
        executor.setMaxPoolSize(10);        // 최대 쓰레드 수
        executor.setQueueCapacity(500);     // 큐 용량
        executor.setThreadNamePrefix("Executor-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new MyAsyncUncaughtExceptionHandler();
    }
}
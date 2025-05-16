package com.ddhouse.chat.handler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class LoggingAsyncExecutor extends ThreadPoolTaskExecutor {

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(() -> {
            System.out.println("➡️ [Callable] 시작 - " + Thread.currentThread().getName() + " / " + task.getClass().getName());
            T result = task.call();
            System.out.println("⬅️️ [Callable] 종료 - " + Thread.currentThread().getName() + " / " + task.getClass().getName());
            return result;
        });
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(() -> {
            System.out.println("➡️ [Thread: " + Thread.currentThread().getName() + "] Runnable 작업 시작!");
            task.run();
            System.out.println("⬅️ [Thread: " + Thread.currentThread().getName() + "] Runnable 작업 종료!");
        });
    }
}



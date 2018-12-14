package com.github.shk0da.micro.main.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.stereotype.Component;
import com.github.shk0da.micro.main.config.AsyncConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public final class CompletableFutureUtil {

    public static final int DEFAULT_TIMEOUT = 10;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    private static final ScheduledThreadPoolExecutor taskScheduler = new ScheduledThreadPoolExecutor(AsyncConfig.AVAILABLE_TASK_THREADS);

    private CompletableFutureUtil() {
        taskScheduler.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("util-task-scheduler-%d").build());
    }

    public static <T> CompletableFuture<T> timeout(String message) {
        return timeoutAfter(message, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    public static <T> CompletableFuture<T> timeoutAfter(final String message, final long timeout, final TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        taskScheduler.schedule(() -> result.completeExceptionally(new TimeoutException("Timeout exceeded: " + message)), timeout, unit);
        return result;
    }
}

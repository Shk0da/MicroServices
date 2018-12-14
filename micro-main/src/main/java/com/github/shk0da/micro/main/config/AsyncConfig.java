package com.github.shk0da.micro.main.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class AsyncConfig {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final int AVAILABLE_TASK_THREADS = AVAILABLE_PROCESSORS * 2;
    public static final int AVAILABLE_TCP_THREADS = (int) (AVAILABLE_TASK_THREADS * 1.5);
    public static final int AVAILABLE_KAFKA_THREADS = AVAILABLE_TASK_THREADS + 1;

    private final static Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    static {
        log.info("Available processors: {}", AVAILABLE_PROCESSORS);
        log.info("Available task threads: {}", AVAILABLE_TASK_THREADS);
        log.info("Available kafka threads: {}", AVAILABLE_KAFKA_THREADS);
        log.info("Available tcp treads: {}", AVAILABLE_TCP_THREADS);
    }

    @Primary
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ConcurrentTaskExecutor taskExecutor = new ConcurrentTaskExecutor();
        taskExecutor.setConcurrentExecutor(Executors.newWorkStealingPool(AVAILABLE_TASK_THREADS));
        return taskExecutor;
    }

    @Bean("cachedThreadPoolExecutor")
    public TaskExecutor cachedThreadPoolExecutor() {
        ConcurrentTaskExecutor taskExecutor = new ConcurrentTaskExecutor();
        taskExecutor.setConcurrentExecutor(Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("main-task-executor-%d").build()
        ));
        return taskExecutor;
    }

    @Bean("taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(AVAILABLE_TASK_THREADS);
        scheduler.setErrorHandler(throwable -> log.error("Scheduled task error: {}", throwable));
        scheduler.setThreadNamePrefix("main-task-scheduler-");
        return scheduler;
    }
}

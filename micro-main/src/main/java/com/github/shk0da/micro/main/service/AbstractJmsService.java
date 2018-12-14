package com.github.shk0da.micro.main.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractJmsService {

    protected final static Logger log = LoggerFactory.getLogger(AbstractJmsService.class);

    @Autowired
    private TaskExecutor executor;

    @Autowired
    protected KafkaService<KafkaMessage> kafkaService;

    @EventListener
    public void listen(KafkaMessage message) {
        CompletableFuture
                .runAsync(router(message), executor)
                .exceptionally(throwable -> {
                    log.error("Failed route message: {}", throwable.getMessage());
                    return null;
                });
    }

    protected abstract Runnable router(KafkaMessage message);

    public void send(KafkaMessage message) {
        kafkaService.sendMessage(message);
    }

    public void send(String topic, KafkaMessage message) {
        kafkaService.sendMessage(topic, message);
    }
}

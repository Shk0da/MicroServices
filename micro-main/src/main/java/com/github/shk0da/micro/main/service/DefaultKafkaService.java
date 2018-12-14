package com.github.shk0da.micro.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import com.github.shk0da.micro.main.config.KafkaConfig;
import com.github.shk0da.micro.main.domain.KafkaMessage;

import java.util.concurrent.CompletableFuture;

public class DefaultKafkaService implements KafkaService<KafkaMessage> {

    private final static Logger log = LoggerFactory.getLogger(DefaultKafkaService.class);

    private final TaskExecutor executor;
    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public DefaultKafkaService(TaskExecutor executor, KafkaTemplate<String, KafkaMessage> kafkaTemplate,
                               ApplicationEventPublisher applicationEventPublisher) {
        this.executor = executor;
        this.kafkaTemplate = kafkaTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void sendMessage(KafkaMessage message) {
        CompletableFuture.runAsync(() -> {
            log.debug("Send Message to '{}': {}", kafkaTemplate.getDefaultTopic(), message);
            kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), message);
        }, executor).exceptionally(throwable -> {
            log.error("Failed send message to '{}': {}", kafkaTemplate.getDefaultTopic(), throwable.getMessage());
            return null;
        });
    }

    @Override
    public void sendMessage(String topic, KafkaMessage message) {
        CompletableFuture.runAsync(() -> {
            log.debug("Send Message to '{}': {}", topic, message);
            kafkaTemplate.send(topic, message);
        }, executor).exceptionally(throwable -> {
            log.error("Failed send message to '{}': {}", topic, throwable.getMessage());
            return null;
        });
    }

    @Override
    public void sendMessage(Message<KafkaMessage> message) {
        CompletableFuture.runAsync(() -> {
            log.debug("Send Message to '{}': {}", message.getHeaders().get(KafkaConfig.DEFAULT_KAFKA_TOPIC_OUT), message.getPayload());
            kafkaTemplate.send(message);
        }, executor).exceptionally(throwable -> {
            log.error("Failed send message to '{}': {}", message.getHeaders().get(KafkaConfig.DEFAULT_KAFKA_TOPIC_OUT), throwable.getMessage());
            return null;
        });
    }

    @KafkaListener(
            topics = {KafkaConfig.DEFAULT_KAFKA_TOPIC_IN},
            groupId = KafkaConfig.DEFAULT_KAFKA_GROUP_ID,
            containerFactory = KafkaConfig.DEFAULT_KAFKA_CONTAINER_FACTORY
    )
    public void listenWithHeaders(
            @Payload KafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        CompletableFuture.runAsync(() -> {
            log.debug("Received Message from '{}' partition {}: {}", topic, partition, message);
            receiveMessage(message);
        }, executor).exceptionally(throwable -> {
            log.error("Failed received Message from '{}' partition {}:", topic, partition, throwable.getMessage());
            return null;
        });
    }

    @Override
    public void receiveMessage(KafkaMessage message) {
        applicationEventPublisher.publishEvent(message);
    }
}

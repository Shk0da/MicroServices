package com.github.shk0da.micro.main.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "kafka.enable", havingValue = "true", matchIfMissing = true)
public interface KafkaService<T> {
    void sendMessage(T message);

    void sendMessage(String topic, T message);

    void sendMessage(Message<T> message);

    void receiveMessage(T message);
}

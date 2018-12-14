package com.github.shk0da.micro.main.config;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(value = "kafka.enable", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    private final static Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    public static final String DEFAULT_KAFKA_CONTAINER_FACTORY = "kafkaListenerContainerFactory";
    public static final String DEFAULT_KAFKA_URL = "${cloudconfig.kafka.url}";
    public static final String DEFAULT_KAFKA_GROUP_ID = "${cloudconfig.kafka.groupId}";
    public static final String DEFAULT_KAFKA_TOPIC_IN = "#{'${cloudconfig.kafka.in}'.split(',')}";
    public static final String DEFAULT_KAFKA_TOPIC_OUT = "${cloudconfig.kafka.out}";

    private final String bootstrapAddress;
    private final String defaultKafkaGroupId;
    private final String defaultKafkaTopicOut;

    public KafkaConfig(
            @Value(DEFAULT_KAFKA_URL) String bootstrapAddress,
            @Value(DEFAULT_KAFKA_GROUP_ID) String defaultKafkaGroupId,
            @Value(DEFAULT_KAFKA_TOPIC_OUT) String defaultKafkaTopicOut
    ) {
        this.bootstrapAddress = bootstrapAddress;
        this.defaultKafkaGroupId = defaultKafkaGroupId;
        this.defaultKafkaTopicOut = defaultKafkaTopicOut;
    }

    @Bean
    public ProducerFactory<String, KafkaMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, KafkaMessage> kafkaTemplate() {
        KafkaTemplate<String, KafkaMessage> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setDefaultTopic(defaultKafkaTopicOut);
        return kafkaTemplate;
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, defaultKafkaGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new JsonDeserializer<>(KafkaMessage.class, KafkaMessage.HandlingTools.objectMapper())
        );
    }

    @Bean(DEFAULT_KAFKA_CONTAINER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(AsyncConfig.AVAILABLE_KAFKA_THREADS);
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setErrorHandler(seekToCurrentErrorHandler());
        return factory;
    }

    @Bean
    public ContainerAwareErrorHandler seekToCurrentErrorHandler() {
        return (thrownException, records, consumer, container) -> {
            log.error("Seek to current after exception: {}", ExceptionUtils.getThrowableList(thrownException));
            if (!records.isEmpty()) {
                Map<TopicPartition, Long> offsets = new LinkedHashMap<>();
                records.forEach(r -> offsets.computeIfAbsent(new TopicPartition(r.topic(), r.partition()), k -> r.offset()));
                offsets.forEach((topicPartition, offset) -> {
                    consumer.seek(topicPartition, offset + 1);
                    log.warn("Partition {} has new offset {}", topicPartition, consumer.position(topicPartition));
                });
            } else {
                consumer.beginningOffsets(consumer.assignment()).forEach((topicPartition, aLong) -> {
                    long position = consumer.position(topicPartition);
                    if (thrownException.getMessage().contains("for partition " + topicPartition + " at offset " + position)) {
                        consumer.seek(topicPartition, position + 1);
                        log.warn("Partition {} has new offset {}", topicPartition, consumer.position(topicPartition));
                    }
                });
            }
        };
    }
}

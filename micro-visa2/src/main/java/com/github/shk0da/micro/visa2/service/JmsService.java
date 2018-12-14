package com.github.shk0da.micro.visa2.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.visa2.config.CacheConfig;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.annotation.KafkaClient;
import com.github.shk0da.micro.main.annotation.ServiceRegistration;
import com.github.shk0da.micro.main.service.AbstractJmsService;

@Service
@KafkaClient
@ServiceRegistration
public class JmsService extends AbstractJmsService {

    private final CacheConfig cacheConfig;

    public JmsService(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    @Override
    protected Runnable router(KafkaMessage message) {
        return () -> {
            switch (message.getType()) {
                case CALCULATE_MAC:
                case VISA2_MESSAGE:
                    cacheConfig.setToReceiverCache(message.getId(), message.getMessage());
                    break;
                default:
                    log.warn("unidentified message: {}", message);
            }
        };
    }
}

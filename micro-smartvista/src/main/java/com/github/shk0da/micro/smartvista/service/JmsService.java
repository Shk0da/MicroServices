package com.github.shk0da.micro.smartvista.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.smartvista.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.annotation.KafkaClient;
import com.github.shk0da.micro.main.annotation.ServiceRegistration;
import com.github.shk0da.micro.main.service.AbstractJmsService;

@Service
@KafkaClient
@ServiceRegistration
public class JmsService extends AbstractJmsService {

    @Autowired
    private CacheConfig cacheConfig;

    @Override
    protected Runnable router(KafkaMessage message) {
        return () -> {
            switch (message.getType()) {
                case LIMIT_CHECK:
                    cacheConfig.setToReceiverCache(message.getId(), message.getMessage());
                    break;
                default:
                    log.warn("unidentified message: {}", message);
            }
        };
    }
}

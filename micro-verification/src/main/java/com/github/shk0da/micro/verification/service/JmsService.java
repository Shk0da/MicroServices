package com.github.shk0da.micro.verification.service;

import com.github.shk0da.micro.main.domain.KafkaMessage;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.annotation.KafkaClient;
import com.github.shk0da.micro.main.service.AbstractJmsService;

@Service
@KafkaClient
public class JmsService extends AbstractJmsService {

    @Override
    protected Runnable router(KafkaMessage message) {
        return () -> {};
    }
}

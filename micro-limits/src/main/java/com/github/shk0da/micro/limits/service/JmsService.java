package com.github.shk0da.micro.limits.service;

import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.annotation.KafkaClient;
import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.main.domain.message.LimitCheck;
import com.github.shk0da.micro.main.service.AbstractJmsService;

@Service
@KafkaClient
public class JmsService extends AbstractJmsService {

    @Override
    protected Runnable router(KafkaMessage message) {
        return () -> {
            switch (message.getType()) {
                case LIMIT_CHECK:
                    LimitCheck limitCheck = (LimitCheck) message.getMessage();
                    limitCheck.setResult(true);
                    // валюта счета не соответсвует вылюте транзакции
                    if (!limitCheck.getCurrencyCodeAccount().equals(limitCheck.getCurrencyCodeTransaction())) {
                        limitCheck.setResult(false);
                    }
                    kafkaService.sendMessage(new KafkaMessage.Builder<>().message(limitCheck).id(message.getId()).build());
                    break;
                default:
                    log.warn("unidentified message: {}", message);
            }
        };
    }
}

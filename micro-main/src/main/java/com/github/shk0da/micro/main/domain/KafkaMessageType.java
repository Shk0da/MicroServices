package com.github.shk0da.micro.main.domain;

import com.github.shk0da.micro.main.domain.message.CalculateMac;
import com.github.shk0da.micro.main.domain.message.CheckMac;
import com.github.shk0da.micro.main.domain.message.LimitCheck;
import com.github.shk0da.micro.main.domain.message.Visa2Message;

import java.util.Arrays;

public enum KafkaMessageType {
    NOT_DEFINED(Object.class),
    SERVICE_REGISTRATION(KafkaMessageService.class),
    VISA2_MESSAGE(Visa2Message.class),
    CHECK_MAC(CheckMac.class),
    CALCULATE_MAC(CalculateMac.class),
    LIMIT_CHECK(LimitCheck.class);

    private Class messageClass;

    KafkaMessageType(Class messageClass) {
        this.messageClass = messageClass;
    }

    public Class getMessageClass() {
        return messageClass;
    }

    public static KafkaMessageType of(Class messageClass) {
        return Arrays.stream(KafkaMessageType.values())
                .parallel()
                .filter(kafkaMessageType -> kafkaMessageType.getMessageClass().equals(messageClass))
                .findFirst()
                .orElse(null);
    }
}

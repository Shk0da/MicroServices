package com.github.shk0da.micro.main.config;

import com.github.shk0da.micro.main.service.KafkaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.github.shk0da.micro.main.domain.KafkaMessage;
import com.github.shk0da.micro.main.domain.KafkaMessageService;

@Configuration
public class ServiceRegistration {

    public ServiceRegistration(KafkaService<KafkaMessage> kafkaService,
                               @Value("${spring.application.name}") String name,
                               @Value("${cloudconfig.kafka.in}") String in,
                               @Value("${cloudconfig.kafka.out}") String out) {
        KafkaMessageService service = new KafkaMessageService(name, in, out);
        @SuppressWarnings("unchecked")
        KafkaMessage message = new KafkaMessage.Builder<KafkaMessageService>().service(service).message(service).build();
        kafkaService.sendMessage(message);
    }
}

package com.github.shk0da.micro.orchestrator.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.github.shk0da.micro.main.domain.KafkaMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
public class ServicesBookConfig {

    private final static Logger log = LoggerFactory.getLogger(ServicesBookConfig.class);

    public enum Service {
        // TODO: актуализировать при изменении структуры сервисов
        balance, cryptography, fees, limits, notice, orchestrator, verification, visa2, smartvista;

        public String getFullName() {
            return "micro-" + name();
        }
    }

    private final IMap<String, KafkaMessageService> registeredServices;

    public ServicesBookConfig(HazelcastInstance hazelcastInstance) {
        registeredServices = hazelcastInstance.getMap("registeredServices");
        if (registeredServices.isEmpty()) {
            initializationServicesDefaultValues();
        }
    }

    private void initializationServicesDefaultValues() {
        registeredServices.lock("initializationServicesDefaultValues");
        try {
            if (!registeredServices.isEmpty()) return;
            for (Service service : Service.values()) {
                registeredServices.put(service.getFullName(), new KafkaMessageService(
                        service.getFullName(), "micro." + service.name() + ".in", "micro." + service.name() + ".out"
                ));
            }
        } finally {
            registeredServices.unlock("initializationServicesDefaultValues");
        }
    }

    public void addService(KafkaMessageService service) {
        registeredServices.lock("addService");
        try {
            registeredServices.put(service.getName(), service);
            log.debug("Service registration: {}", service);
        } finally {
            registeredServices.unlock("addService");
        }
    }

    @Nullable
    public KafkaMessageService getService(Service service) {
        return registeredServices.getOrDefault(service.getFullName(), null);
    }
}

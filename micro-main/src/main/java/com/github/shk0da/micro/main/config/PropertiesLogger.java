package com.github.shk0da.micro.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class PropertiesLogger {

    private final static Logger log = LoggerFactory.getLogger(PropertiesLogger.class);

    @Autowired
    private AbstractEnvironment environment;

    @PostConstruct
    public void printProperties() {
        Map<String, String> properties = new HashMap<>();
        environment.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof PropertiesPropertySource
                    || propertySource instanceof OriginTrackedMapPropertySource
                    || propertySource instanceof CompositePropertySource
                    || propertySource instanceof SimpleCommandLinePropertySource) {
                for (String propertyName : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                    try {
                        properties.put(propertyName, environment.getProperty(propertyName));
                    } catch (Exception ex) {
                        log.warn(ex.getMessage());
                    }
                }
            }
        });

        properties.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new))
                .forEach((k, v) -> log.info("{}={}", k, v));
    }
}

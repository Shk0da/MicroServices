package com.github.shk0da.micro.main.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class ApplicationContextProvider {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Nullable
    public static Environment getEnvironment() {
        return getApplicationContext() != null ? getApplicationContext().getEnvironment() : null;
    }

    @Nullable
    public static String getProperty(String key) {
        return getEnvironment() != null ? getEnvironment().getProperty(key) : null;
    }

    public static String getServiceName() {
        return Optional.ofNullable(getProperty("spring.application.name")).orElse("UNKNOWN");
    }

    public static String getServiceTopicIn() {
        String topicIn = Optional.ofNullable(getProperty("cloudconfig.kafka.in")).orElse(getServiceName() + ".in");
        return Arrays.stream(topicIn.split(",")).findFirst().orElse(null);
    }

    public static String getServiceTopicOut() {
        String topicOut = Optional.ofNullable(getProperty("cloudconfig.kafka.out")).orElse(getServiceName() + ".out");
        return Arrays.stream(topicOut.split(",")).findFirst().orElse(null);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextProvider.applicationContext = applicationContext;
    }
}

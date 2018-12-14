package com.github.shk0da.micro.main.annotation;

import com.github.shk0da.micro.main.config.AsyncConfig;
import com.github.shk0da.micro.main.config.KafkaConfig;
import com.github.shk0da.micro.main.service.DefaultKafkaService;
import com.github.shk0da.micro.main.provider.ApplicationContextProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        AsyncConfig.class,
        KafkaConfig.class,
        DefaultKafkaService.class,
        ApplicationContextProvider.class
})
public @interface KafkaClient {
}

package com.github.shk0da.micro.cloudconfig.annotation;

import com.github.shk0da.micro.cloudconfig.config.LoggingConfiguration;
import com.github.shk0da.micro.cloudconfig.config.MetricsConfiguration;
import com.github.shk0da.micro.cloudconfig.config.WebConfigurer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(
        {LoggingConfiguration.class,
                MetricsConfiguration.class,
                WebConfigurer.class}
)
public @interface CloudConfigClient {
}

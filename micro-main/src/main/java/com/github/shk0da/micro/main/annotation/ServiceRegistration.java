package com.github.shk0da.micro.main.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        com.github.shk0da.micro.main.config.ServiceRegistration.class,
})
public @interface ServiceRegistration {
}

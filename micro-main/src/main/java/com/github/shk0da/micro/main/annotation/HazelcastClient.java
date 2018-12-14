package com.github.shk0da.micro.main.annotation;

import com.github.shk0da.micro.main.config.HazelcastConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        HazelcastConfig.class
})
public @interface HazelcastClient {
}

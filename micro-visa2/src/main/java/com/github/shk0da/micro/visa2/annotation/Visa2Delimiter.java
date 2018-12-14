package com.github.shk0da.micro.visa2.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Visa2Delimiter {
    byte value() default (byte) 0x1C;
}

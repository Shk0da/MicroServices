package com.github.shk0da.micro.visa2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Visa2Field {

    enum Type {STRING, INTEGER, LONG, MIX}

    int length();

    Type type() default Type.MIX;

    byte delimiter() default (byte) 0x1C;

    String description() default "";
}

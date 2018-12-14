package com.github.shk0da.micro.smartvista.domain;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IsoMsg {
    IsoMessageType type();
}

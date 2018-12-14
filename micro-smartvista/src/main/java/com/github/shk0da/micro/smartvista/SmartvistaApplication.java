package com.github.shk0da.micro.smartvista;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.shk0da.micro.cloudconfig.annotation.CloudConfigClient;

@CloudConfigClient
@SpringBootApplication
public class SmartvistaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartvistaApplication.class, args);
    }
}

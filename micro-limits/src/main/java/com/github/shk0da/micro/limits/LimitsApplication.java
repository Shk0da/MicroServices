package com.github.shk0da.micro.limits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.shk0da.micro.cloudconfig.annotation.CloudConfigClient;

@CloudConfigClient
@SpringBootApplication
public class LimitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LimitsApplication.class, args);
    }
}

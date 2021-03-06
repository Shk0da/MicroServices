package com.github.shk0da.micro.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.shk0da.micro.cloudconfig.annotation.CloudConfigClient;

@CloudConfigClient
@SpringBootApplication
public class VerificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerificationApplication.class, args);
    }
}

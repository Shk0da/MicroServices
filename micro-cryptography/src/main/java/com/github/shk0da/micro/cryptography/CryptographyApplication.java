package com.github.shk0da.micro.cryptography;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.shk0da.micro.cloudconfig.annotation.CloudConfigClient;
import com.github.shk0da.micro.main.annotation.PropertiesLogger;

@PropertiesLogger
@CloudConfigClient
@SpringBootApplication
public class CryptographyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptographyApplication.class, args);
    }
}

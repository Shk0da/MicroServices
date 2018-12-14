package com.github.shk0da.micro.balance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.shk0da.micro.cloudconfig.annotation.CloudConfigClient;

@CloudConfigClient
@SpringBootApplication
public class BalanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BalanceApplication.class, args);
    }
}

package com.github.shk0da.micro.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.github.shk0da.micro.cloudconfig.annotation.CloudConfigClient;
import com.github.shk0da.micro.main.annotation.HazelcastClient;
import com.github.shk0da.micro.main.annotation.PropertiesLogger;

@HazelcastClient
@PropertiesLogger
@CloudConfigClient
@SpringBootApplication
public class OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }
}

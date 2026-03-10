package com.example.cloudfileprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudFileProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudFileProcessingApplication.class, args);
    }
}

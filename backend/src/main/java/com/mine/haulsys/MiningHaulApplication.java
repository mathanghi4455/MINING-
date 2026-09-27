package com.mine.haulsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableScheduling
@EnableMethodSecurity
public class MiningHaulApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiningHaulApplication.class, args);
    }
}

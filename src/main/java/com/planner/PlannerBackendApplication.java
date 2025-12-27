package com.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PlannerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlannerBackendApplication.class, args);
    }

}

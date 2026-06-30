package com.primesys.adminserviceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService reportExecutor() {
        // 8 threads = good for DB + CPU mix
        return Executors.newFixedThreadPool(8);
    }
}

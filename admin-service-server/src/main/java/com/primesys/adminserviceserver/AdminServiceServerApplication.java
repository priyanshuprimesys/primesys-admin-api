package com.primesys.adminserviceserver;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = { "com.primesys" })
// @EnableScheduling
public class AdminServiceServerApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceServerApplication.class);

    @Value("${app.springdocUrl:}")
    private String springDocUrl;

    @Value("${app.springdocJSON:}")
    private String springdocJSON;

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceServerApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Spring DOC JSON: {}", springdocJSON);
        logger.info("Spring DOC URL: {}", springDocUrl);
    }
}

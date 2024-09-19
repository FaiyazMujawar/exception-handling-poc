package com.example.exceptionhandlingpoc;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class ExceptionHandlingPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExceptionHandlingPocApplication.class, args);
    }

}
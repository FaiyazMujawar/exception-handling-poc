package com.rheumera.poc;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rheumera.poc.anoop.PlayerReader;

@SpringBootApplication
@EnableBatchProcessing
public class ExceptionHandlingPocApplication implements CommandLineRunner {

	@Autowired
	private PlayerReader reader;
	public static void main(String[] args) {
		SpringApplication.run(ExceptionHandlingPocApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reader.read();
	}
}
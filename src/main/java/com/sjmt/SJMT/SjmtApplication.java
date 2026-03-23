package com.sjmt.SJMT;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.sjmt.SJMT")
public class SjmtApplication {

	public static void main(String[] args) {
		// Set timezone BEFORE Spring Boot starts
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		SpringApplication.run(SjmtApplication.class, args);

	}
}
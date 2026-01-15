package com.sjmt.SJMT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class SjmtApplication {

	public static void main(String[] args) {
		// Set timezone BEFORE Spring Boot starts
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		SpringApplication.run(SjmtApplication.class, args);
	}
}
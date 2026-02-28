package com.cement.telegrampdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramPdfSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramPdfSenderApplication.class, args);
	}

}

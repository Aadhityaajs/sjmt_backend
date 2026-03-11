package com.cement.telegrampdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;

@Configuration
@Validated
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.chatId}")
    private String chatId;

    @PostConstruct
    public void validateConfiguration() {
        if (botToken == null || botToken.isEmpty() || botToken.equals("YOUR_BOT_TOKEN_HERE")) {
            throw new IllegalStateException("Telegram bot token is not configured properly");
        }

        if (botUsername == null || botUsername.isEmpty() || botUsername.equals("YOUR_BOT_USERNAME_HERE")) {
            throw new IllegalStateException("Telegram bot username is not configured properly");
        }

        if (chatId == null || chatId.isEmpty() || chatId.equals("YOUR_CHAT_ID_HERE")) {
            throw new IllegalStateException("Telegram chat ID is not configured properly");
        }

        System.out.println("✅ Telegram bot configuration validated successfully");
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getChatId() {
        return chatId;
    }
}
package com.cement.telegrampdf.service;

import com.cement.telegrampdf.config.TelegramBotConfig;
import com.cement.telegrampdf.dto.SendPdfResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private TelegramBotConfig botConfig;

    @Autowired
    private PdfFileService pdfFileService;

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();

            logger.info("Received message from @{} ({}): {}", username, chatId, messageText);

            switch (messageText) {
                case "/start":
                    sendMessage(chatId, "🤖 Welcome to Cement Project PDF Bot!\n\n" +
                            "I will send you daily PDF reports at 8:00 PM.\n\n" +
                            "Available commands:\n" +
                            "/chatid - Get your chat ID\n" +
                            "/status - Check system status\n" +
                            "/help - Show this message");
                    break;

                case "/chatid":
                    sendMessage(chatId, "📱 Your Chat ID: " + chatId);
                    break;

                case "/status":
                    handleStatusCommand(chatId);
                    break;

                case "/help":
                    sendMessage(chatId, "📚 Available Commands:\n\n" +
                            "/start - Start the bot\n" +
                            "/chatid - Get your chat ID\n" +
                            "/status - Check system status\n" +
                            "/help - Show this message");
                    break;

                default:
                    sendMessage(chatId, "❓ Unknown command. Type /help for available commands.");
            }
        }
    }

    /**
     * Handle status command
     */
    private void handleStatusCommand(Long chatId) {
        boolean accessible = pdfFileService.isStorageAccessible();
        List<File> todaysPdfs = pdfFileService.getTodaysPdfFiles();
        List<File> allPdfs = pdfFileService.getAllPdfFiles();

        String status = "📊 System Status\n\n" +
                "📁 Storage: " + (accessible ? "✅ Accessible" : "❌ Not Accessible") + "\n" +
                "📄 Total PDFs: " + allPdfs.size() + "\n" +
                "📅 Today's PDFs: " + todaysPdfs.size() + "\n" +
                "🕐 Current Time: " + LocalDateTime.now().format(TIME_FORMATTER) + "\n" +
                "📆 Date: " + LocalDate.now().format(DATE_FORMATTER);

        sendMessage(chatId, status);
    }

    /**
     * Send a text message to specific chat
     */
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
            logger.info("Message sent successfully to chat: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Send a text message to configured default chat
     */
    public void sendMessage(String text) {
        sendMessage(Long.parseLong(botConfig.getChatId()), text);
    }

    /**
     * Send a single PDF file
     */
    public boolean sendPdfFile(File pdfFile) {
        return sendPdfFile(pdfFile, createPdfCaption(pdfFile));
    }

    /**
     * Send a PDF file with custom caption
     */
    public boolean sendPdfFile(File pdfFile, String caption) {
        logger.info("Attempting to send PDF: {}", pdfFile.getName());

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(botConfig.getChatId());
        sendDocument.setDocument(new InputFile(pdfFile));
        sendDocument.setCaption(caption);

        try {
            execute(sendDocument);
            logger.info("✅ PDF sent successfully: {}", pdfFile.getName());
            return true;
        } catch (TelegramApiException e) {
            logger.error("❌ Failed to send PDF {}: {}", pdfFile.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send multiple PDF files
     */
    public SendPdfResponse sendMultiplePdfFiles(List<File> pdfFiles) {
        SendPdfResponse response = new SendPdfResponse();
        response.setTotalFiles(pdfFiles.size());

        List<String> sentFileNames = new ArrayList<>();
        List<String> failedFileNames = new ArrayList<>();

        if (pdfFiles.isEmpty()) {
            sendMessage("📭 No PDF files to send for today (" + LocalDate.now().format(DATE_FORMATTER) + ")");
            response.setSentSuccessfully(0);
            response.setFailed(0);
            return response;
        }

        sendMessage("📤 Starting to send " + pdfFiles.size() + " PDF file(s)...");

        int successCount = 0;
        int failCount = 0;

        for (File pdfFile : pdfFiles) {
            pdfFileService.logFileInfo(pdfFile);

            boolean sent = sendPdfFile(pdfFile);

            if (sent) {
                successCount++;
                sentFileNames.add(pdfFile.getName());
            } else {
                failCount++;
                failedFileNames.add(pdfFile.getName());
            }

            // Delay between sends to avoid rate limiting
            if (pdfFiles.size() > 1) {
                try {
                    Thread.sleep(1000); // 1 second delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Thread interrupted during delay");
                }
            }
        }

        response.setSentSuccessfully(successCount);
        response.setFailed(failCount);
        response.setSentFileNames(sentFileNames);
        response.setFailedFileNames(failedFileNames);

        // Send summary message
        String summary = "✅ PDF Sending Complete!\n\n" +
                "📊 Summary:\n" +
                "• Total: " + pdfFiles.size() + "\n" +
                "• Sent: " + successCount + "\n" +
                "• Failed: " + failCount;

        if (failCount > 0) {
            summary += "\n\n❌ Failed files:\n";
            for (String failedFile : failedFileNames) {
                summary += "• " + failedFile + "\n";
            }
        }

        sendMessage(summary);

        return response;
    }

    /**
     * Create caption for PDF file
     */
    private String createPdfCaption(File pdfFile) {
        return "📄 " + pdfFile.getName() + "\n" +
                "📅 Date: " + LocalDate.now().format(DATE_FORMATTER) + "\n" +
                "📦 Size: " + pdfFileService.formatFileSize(pdfFile.length()) + "\n" +
                "🏢 Cement Project Report";
    }
}
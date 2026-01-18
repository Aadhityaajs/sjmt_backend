package com.cement.telegrampdf.service;

import com.cement.telegrampdf.dto.SendPdfResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@ConditionalOnProperty(name = "scheduler.pdf.send.enabled", havingValue = "true", matchIfMissing = true)
public class PdfSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(PdfSchedulerService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PdfFileService pdfFileService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Value("${scheduler.pdf.send.cron}")
    private String cronExpression;

    /**
     * Main scheduled task - Sends today's PDFs at configured time (default 8:00 PM)
     * Cron expression from properties: ${scheduler.pdf.send.cron}
     */
    @Scheduled(cron = "${scheduler.pdf.send.cron}", zone = "${scheduler.timezone:Asia/Kolkata}")
    public void sendDailyPdfFiles() {
        LocalDateTime executionTime = LocalDateTime.now();

        logger.info("=".repeat(80));
        logger.info("📅 DAILY PDF SCHEDULER TRIGGERED");
        logger.info("🕐 Execution Time: {}", executionTime.format(FORMATTER));
        logger.info("⚙️  Cron Expression: {}", cronExpression);
        logger.info("=".repeat(80));

        try {
            // Check if storage is accessible
            if (!pdfFileService.isStorageAccessible()) {
                String errorMsg = "❌ ERROR: PDF storage directory is not accessible";
                logger.error(errorMsg);
                telegramBotService.sendMessage(errorMsg);
                return;
            }

            // Fetch today's PDF files
            List<File> todaysPdfs = pdfFileService.getTodaysPdfFiles();

            logger.info("📊 Files Summary:");
            logger.info("  - Total files found today: {}", todaysPdfs.size());

            if (todaysPdfs.isEmpty()) {
                logger.warn("📭 No PDF files found for today");
                telegramBotService.sendMessage(
                        "📭 Daily PDF Report\n\n" +
                                "No new PDF files were uploaded today.\n" +
                                "Date: " + LocalDateTime.now().toLocalDate()
                );
            } else {
                logger.info("📤 Preparing to send {} PDF file(s)", todaysPdfs.size());

                // Log each file
                for (int i = 0; i < todaysPdfs.size(); i++) {
                    File pdf = todaysPdfs.get(i);
                    logger.info("  {}. {} ({})",
                            i + 1,
                            pdf.getName(),
                            pdfFileService.formatFileSize(pdf.length())
                    );
                }

                // Send PDFs via Telegram
                SendPdfResponse response = telegramBotService.sendMultiplePdfFiles(todaysPdfs);

                logger.info("✅ PDF Sending Completed:");
                logger.info("  - Total: {}", response.getTotalFiles());
                logger.info("  - Sent Successfully: {}", response.getSentSuccessfully());
                logger.info("  - Failed: {}", response.getFailed());

                if (response.getFailed() > 0) {
                    logger.error("❌ Failed to send {} file(s)", response.getFailed());
                    for (String failedFile : response.getFailedFileNames()) {
                        logger.error("  - {}", failedFile);
                    }
                }
            }

            logger.info("✅ Daily PDF scheduler task completed successfully");

        } catch (Exception e) {
            logger.error("❌ CRITICAL ERROR in daily PDF scheduler", e);

            try {
                telegramBotService.sendMessage(
                        "❌ SYSTEM ERROR\n\n" +
                                "An error occurred during daily PDF sending:\n" +
                                e.getMessage() + "\n\n" +
                                "Please check application logs for details."
                );
            } catch (Exception notificationError) {
                logger.error("Failed to send error notification to Telegram", notificationError);
            }
        } finally {
            logger.info("=".repeat(80));
        }
    }

    /**
     * Test scheduler - Runs every hour (for testing)
     * Comment out or remove in production
     */
    // @Scheduled(cron = "0 0 * * * *")
    public void hourlyHealthCheck() {
        logger.debug("Health check: System is running. Storage accessible: {}",
                pdfFileService.isStorageAccessible());
    }
}
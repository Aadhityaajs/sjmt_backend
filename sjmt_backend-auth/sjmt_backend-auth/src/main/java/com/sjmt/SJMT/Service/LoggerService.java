package com.sjmt.SJMT.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sjmt.SJMT.Entity.ApiLog;
import com.sjmt.SJMT.Repository.ApiLogRepository;

/**
 * Logger Service
 * Handles logging to database and text file
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class LoggerService {

    private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);

    @Autowired
    private ApiLogRepository apiLogRepository;

    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log API call to database and file (async)
     */
    @Async
    public void logApiCall(LocalDateTime timestamp, String username, String apiEndpoint, Integer responseCode) {
        try {
            logger.info("=== LOGGER SERVICE CALLED ===");
            logger.info("Params: {} | {} | {} | {}", timestamp, username, apiEndpoint, responseCode);

            // Create ApiLog object
            ApiLog apiLog = new ApiLog(timestamp, username, apiEndpoint, responseCode);
            logger.info("ApiLog object created: {}", apiLog);

            // Save to database
            logger.info("Saving to database...");
            saveToDatabase(apiLog);

            // Write to file
            logger.info("Writing to file...");
            writeToFile(apiLog);

            logger.info("=== LOGGER SERVICE COMPLETED ===");

        } catch (Exception e) {
            logger.error("Error logging API call: {}", e.getMessage(), e);
        }
    }

    /**
     * Save log to database
     */
    private void saveToDatabase(ApiLog apiLog) {
        try {
            apiLogRepository.save(apiLog);
            logger.debug("Saved to database: {}", apiLog);
        } catch (Exception e) {
            logger.error("Failed to save log to database: {}", e.getMessage());
        }
    }

    /**
     * Write log to text file
     */
    private void writeToFile(ApiLog apiLog) {
        try {
            logger.info("=== ATTEMPTING TO WRITE TO FILE ===");

            // Create logs directory if not exists
            File logDir = new File(LOG_DIR);
            logger.info("Log directory path: {}", logDir.getAbsolutePath());

            if (!logDir.exists()) {
                logger.info("Directory doesn't exist, creating...");
                boolean created = logDir.mkdirs();
                logger.info("Directory created successfully: {}", created);
            } else {
                logger.info("Directory already exists");
            }

            // Generate filename with date
            String fileName = String.format("api-logs-%s.txt",
                    apiLog.getTimestamp().format(FILE_DATE_FORMAT));
            logger.info("Generated filename: {}", fileName);

            File logFile = new File(logDir, fileName);
            logger.info("Full file path: {}", logFile.getAbsolutePath());
            logger.info("File exists before write: {}", logFile.exists());

            // Format log entry
            String logEntry = formatLogEntry(apiLog);
            logger.info("Log entry to write: {}", logEntry);

            // Append to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logEntry);
                writer.newLine();
                writer.flush(); // Force write to disk
            }

            logger.info("File exists after write: {}", logFile.exists());
            logger.info("=== FILE WRITE COMPLETED SUCCESSFULLY ===");

        } catch (IOException e) {
            logger.error("Failed to write log to file: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    /**
     * Format log entry for text file
     * Format: timestamp | username | api_endpoint | response_code
     */
    private String formatLogEntry(ApiLog apiLog) {
        return String.format("%s | %s | %s | %d",
                apiLog.getTimestamp().format(LOG_TIMESTAMP_FORMAT),
                apiLog.getUsername() != null ? apiLog.getUsername() : "ANONYMOUS",
                apiLog.getApiEndpoint(),
                apiLog.getResponseCode()
        );
    }
}
package com.cement.telegrampdf.controller;

import com.cement.telegrampdf.dto.ApiResponse;
import com.cement.telegrampdf.dto.PdfFileInfo;
import com.cement.telegrampdf.dto.SendPdfResponse;
import com.cement.telegrampdf.service.PdfFileService;
import com.cement.telegrampdf.service.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);

    @Autowired
    private PdfFileService pdfFileService;

    @Autowired
    private TelegramBotService telegramBotService;

    /**
     * Health check endpoint
     * GET /api/pdf/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        logger.info("Health check endpoint called");

        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("storageAccessible", pdfFileService.isStorageAccessible());
        healthData.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(
                new ApiResponse(true, "System is healthy", healthData)
        );
    }

    /**
     * Get system status
     * GET /api/pdf/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getSystemStatus() {
        logger.info("Status endpoint called");

        try {
            boolean storageAccessible = pdfFileService.isStorageAccessible();
            List<File> todaysPdfs = pdfFileService.getTodaysPdfFiles();
            List<File> allPdfs = pdfFileService.getAllPdfFiles();

            Map<String, Object> statusData = new HashMap<>();
            statusData.put("storageAccessible", storageAccessible);
            statusData.put("storageLocation", pdfFileService.isStorageAccessible() ?
                    new File(System.getProperty("user.dir")).getAbsolutePath() : "N/A");
            statusData.put("totalPdfFiles", allPdfs.size());
            statusData.put("todaysPdfFiles", todaysPdfs.size());
            statusData.put("currentDate", java.time.LocalDate.now());
            statusData.put("currentTime", java.time.LocalTime.now());

            return ResponseEntity.ok(
                    new ApiResponse(true, "System status retrieved successfully", statusData)
            );
        } catch (Exception e) {
            logger.error("Error retrieving system status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Get today's PDF files information
     * GET /api/pdf/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse> getTodaysPdfInfo() {
        logger.info("Get today's PDFs endpoint called");

        try {
            List<PdfFileInfo> pdfInfoList = pdfFileService.getTodaysPdfFileInfo();

            return ResponseEntity.ok(
                    new ApiResponse(true,
                            "Found " + pdfInfoList.size() + " PDF file(s) for today",
                            pdfInfoList)
            );
        } catch (Exception e) {
            logger.error("Error retrieving today's PDFs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Get all PDF files information
     * GET /api/pdf/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllPdfInfo() {
        logger.info("Get all PDFs endpoint called");

        try {
            List<File> allPdfs = pdfFileService.getAllPdfFiles();
            List<PdfFileInfo> pdfInfoList = allPdfs.stream()
                    .map(pdfFileService::convertToPdfFileInfo)
                    .toList();

            return ResponseEntity.ok(
                    new ApiResponse(true,
                            "Found " + pdfInfoList.size() + " total PDF file(s)",
                            pdfInfoList)
            );
        } catch (Exception e) {
            logger.error("Error retrieving all PDFs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Manually trigger sending today's PDFs
     * POST /api/pdf/send-today
     */
    @PostMapping("/send-today")
    public ResponseEntity<ApiResponse> sendTodaysPdfs() {
        logger.info("Manual send today's PDFs triggered");

        try {
            List<File> todaysPdfs = pdfFileService.getTodaysPdfFiles();

            if (todaysPdfs.isEmpty()) {
                return ResponseEntity.ok(
                        new ApiResponse(false, "No PDF files found for today")
                );
            }

            SendPdfResponse response = telegramBotService.sendMultiplePdfFiles(todaysPdfs);

            return ResponseEntity.ok(
                    new ApiResponse(true,
                            "PDFs sent successfully",
                            response)
            );
        } catch (Exception e) {
            logger.error("Error sending today's PDFs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Send test message to Telegram
     * POST /api/pdf/test-message
     */
    @PostMapping("/test-message")
    public ResponseEntity<ApiResponse> sendTestMessage(
            @RequestParam(required = false, defaultValue = "🧪 Test message from Cement PDF Bot")
            String message) {
        logger.info("Test message endpoint called");

        try {
            telegramBotService.sendMessage(message);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Test message sent successfully")
            );
        } catch (Exception e) {
            logger.error("Error sending test message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Send all PDFs (for testing only - not for production use)
     * POST /api/pdf/send-all
     */
    @PostMapping("/send-all")
    public ResponseEntity<ApiResponse> sendAllPdfs() {
        logger.warn("Send all PDFs triggered - This should only be used for testing!");

        try {
            List<File> allPdfs = pdfFileService.getAllPdfFiles();

            if (allPdfs.isEmpty()) {
                return ResponseEntity.ok(
                        new ApiResponse(false, "No PDF files found in directory")
                );
            }

            SendPdfResponse response = telegramBotService.sendMultiplePdfFiles(allPdfs);

            return ResponseEntity.ok(
                    new ApiResponse(true,
                            "All PDFs sent successfully",
                            response)
            );
        } catch (Exception e) {
            logger.error("Error sending all PDFs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }
}
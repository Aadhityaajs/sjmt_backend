package com.cement.telegrampdf.service;

import com.cement.telegrampdf.config.PdfStorageConfig;
import com.cement.telegrampdf.dto.PdfFileInfo;
//import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfFileService {

    private static final Logger logger = LoggerFactory.getLogger(PdfFileService.class);

    @Autowired
    private PdfStorageConfig pdfStorageConfig;

    /**
     * Get all PDF files uploaded today based on file modification date
     */
    public List<File> getTodaysPdfFiles() {
        logger.info("Fetching today's PDF files from: {}", pdfStorageConfig.getStoragePath());

        File directory = new File(pdfStorageConfig.getStoragePath());

        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("PDF directory does not exist or is not accessible: {}", directory.getAbsolutePath());
            return new ArrayList<>();
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long startTimestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        logger.debug("Filtering files between {} and {}", startOfDay, endOfDay);

        FileFilter pdfFilter = file ->
                file.isFile() &&
                        file.getName().toLowerCase().endsWith(pdfStorageConfig.getFileExtension());

        File[] allPdfFiles = directory.listFiles(pdfFilter);

        if (allPdfFiles == null || allPdfFiles.length == 0) {
            logger.warn("No PDF files found in directory");
            return new ArrayList<>();
        }

        List<File> todaysPdfs = Arrays.stream(allPdfFiles)
                .filter(file -> {
                    long lastModified = file.lastModified();
                    return lastModified >= startTimestamp && lastModified < endTimestamp;
                })
                .collect(Collectors.toList());

        logger.info("Found {} PDF file(s) uploaded today out of {} total files",
                todaysPdfs.size(), allPdfFiles.length);

        return todaysPdfs;
    }

    /**
     * Get all PDF files in the directory (for testing purposes)
     */
    public List<File> getAllPdfFiles() {
        logger.info("Fetching all PDF files from: {}", pdfStorageConfig.getStoragePath());

        File directory = new File(pdfStorageConfig.getStoragePath());

        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("PDF directory does not exist: {}", directory.getAbsolutePath());
            return new ArrayList<>();
        }

        FileFilter pdfFilter = file ->
                file.isFile() &&
                        file.getName().toLowerCase().endsWith(pdfStorageConfig.getFileExtension());

        File[] pdfFiles = directory.listFiles(pdfFilter);

        if (pdfFiles == null) {
            return new ArrayList<>();
        }

        logger.info("Found {} total PDF file(s)", pdfFiles.length);
        return Arrays.asList(pdfFiles);
    }

    /**
     * Convert File to PdfFileInfo DTO
     */
    public PdfFileInfo convertToPdfFileInfo(File file) {
        PdfFileInfo info = new PdfFileInfo();
        info.setFileName(file.getName());
        info.setFilePath(file.getAbsolutePath());
        info.setFileSizeBytes(file.length());
        info.setFileSizeFormatted(formatFileSize(file.length()));

        LocalDateTime lastModified = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.lastModified()),
                ZoneId.systemDefault()
        );
        info.setLastModified(lastModified);

        return info;
    }

    /**
     * Get detailed information about today's PDFs
     */
    public List<PdfFileInfo> getTodaysPdfFileInfo() {
        return getTodaysPdfFiles().stream()
                .map(this::convertToPdfFileInfo)
                .collect(Collectors.toList());
    }

    /**
     * Check if storage directory is accessible
     */
    public boolean isStorageAccessible() {
        File directory = new File(pdfStorageConfig.getStoragePath());
        boolean accessible = directory.exists() && directory.isDirectory() && directory.canRead();

        if (!accessible) {
            logger.error("Storage directory is not accessible: {}", pdfStorageConfig.getStoragePath());
        }

        return accessible;
    }

    /**
     * Format file size in human-readable format
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Log file information
     */
    public void logFileInfo(File file) {
        logger.info("File Details:");
        logger.info("  Name: {}", file.getName());
        logger.info("  Path: {}", file.getAbsolutePath());
        logger.info("  Size: {}", formatFileSize(file.length()));
        logger.info("  Last Modified: {}",
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()
                )
        );
    }
}
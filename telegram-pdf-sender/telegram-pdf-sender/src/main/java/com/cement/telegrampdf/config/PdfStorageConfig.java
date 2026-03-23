package com.cement.telegrampdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class PdfStorageConfig {

    @Value("${pdf.storage.path}")
    private String storagePath;

    @Value("${pdf.file.extension}")
    private String fileExtension;

    @PostConstruct
    public void validateStoragePath() {
        File directory = new File(storagePath);

        if (!directory.exists()) {
            System.err.println("⚠️ WARNING: PDF storage directory does not exist: " + storagePath);
            System.err.println("⚠️ Attempting to create directory...");

            if (directory.mkdirs()) {
                System.out.println("✅ Directory created successfully: " + storagePath);
            } else {
                throw new IllegalStateException("Failed to create PDF storage directory: " + storagePath);
            }
        } else if (!directory.isDirectory()) {
            throw new IllegalStateException("PDF storage path is not a directory: " + storagePath);
        } else if (!directory.canRead()) {
            throw new IllegalStateException("Cannot read PDF storage directory: " + storagePath);
        } else {
            System.out.println("✅ PDF storage directory validated: " + storagePath);
        }
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
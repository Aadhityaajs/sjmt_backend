
// PdfFileInfo.java
package com.cement.telegrampdf.dto;

import java.time.LocalDateTime;

public class PdfFileInfo {

    private String fileName;
    private String filePath;
    private long fileSizeBytes;
    private String fileSizeFormatted;
    private LocalDateTime lastModified;

    public PdfFileInfo() {
    }

    public PdfFileInfo(String fileName, String filePath, long fileSizeBytes,
                       String fileSizeFormatted, LocalDateTime lastModified) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.fileSizeFormatted = fileSizeFormatted;
        this.lastModified = lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getFileSizeFormatted() {
        return fileSizeFormatted;
    }

    public void setFileSizeFormatted(String fileSizeFormatted) {
        this.fileSizeFormatted = fileSizeFormatted;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}



// SendPdfResponse.java
package com.cement.telegrampdf.dto;

import java.util.List;

public class SendPdfResponse {

    private int totalFiles;
    private int sentSuccessfully;
    private int failed;
    private List<String> sentFileNames;
    private List<String> failedFileNames;

    public SendPdfResponse() {
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getSentSuccessfully() {
        return sentSuccessfully;
    }

    public void setSentSuccessfully(int sentSuccessfully) {
        this.sentSuccessfully = sentSuccessfully;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<String> getSentFileNames() {
        return sentFileNames;
    }

    public void setSentFileNames(List<String> sentFileNames) {
        this.sentFileNames = sentFileNames;
    }

    public List<String> getFailedFileNames() {
        return failedFileNames;
    }

    public void setFailedFileNames(List<String> failedFileNames) {
        this.failedFileNames = failedFileNames;
    }
}
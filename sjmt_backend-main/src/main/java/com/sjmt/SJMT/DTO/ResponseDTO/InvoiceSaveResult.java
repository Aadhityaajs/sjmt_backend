package com.sjmt.SJMT.DTO.ResponseDTO;

import java.util.List;

/**
 * Result of saving extracted invoice items to inventory.
 * Contains both successfully saved items and per-item failure messages.
 */
public class InvoiceSaveResult {

    private List<Integer> savedInventoryIds;
    private int successCount;
    private int failureCount;
    private List<String> failureMessages;

    public InvoiceSaveResult(List<Integer> savedInventoryIds, int successCount,
                              int failureCount, List<String> failureMessages) {
        this.savedInventoryIds = savedInventoryIds;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failureMessages = failureMessages;
    }

    public List<Integer> getSavedInventoryIds() { return savedInventoryIds; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public List<String> getFailureMessages() { return failureMessages; }
}

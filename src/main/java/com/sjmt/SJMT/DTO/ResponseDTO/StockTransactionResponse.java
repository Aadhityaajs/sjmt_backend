package com.sjmt.SJMT.DTO.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A unified view of stock movements (both purchases and sales) for a product.
 * Used in the transaction history endpoint.
 */
public class StockTransactionResponse {

    public enum TransactionType { PURCHASE, SALE }

    private Integer transactionId;
    private TransactionType type;       // PURCHASE or SALE
    private BigDecimal quantity;
    private BigDecimal rate;            // purchaseRate for PURCHASE, sellingRate for SALE
    private BigDecimal totalValue;      // quantity * rate
    private LocalDate transactionDate;
    private String party;               // supplierName / manufacturerName for PURCHASE, customerName for SALE
    private String reference;           // invoiceNumber / notes
    private String recordedBy;
    private LocalDateTime createdAt;

    public StockTransactionResponse() {
    }

    // Getters and Setters
    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.sjmt.SJMT.DTO.ResponseDTO;

import com.sjmt.SJMT.Entity.PaymentModeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BillPaymentResponse {

    private Integer id;
    private Integer billId;
    private String billNumber;
    private BigDecimal amount;
    private PaymentModeEnum paymentMode;
    private String paymentReference;
    private LocalDate paymentDate;
    private String entryType;
    private String reversalReason;
    private String recordedBy;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentModeEnum getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentModeEnum paymentMode) { this.paymentMode = paymentMode; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.sjmt.SJMT.DTO.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.GstPercentageEnum;
import com.sjmt.SJMT.Entity.PaymentModeEnum;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

public class ExpenseResponseDTO {

    private Integer expenseId;
    private String title;
    private Integer categoryId;
    private String categoryName;
    private LocalDate expenseDate;
    private BigDecimal amount;
    private boolean isGstApplicable;
    private GstPercentageEnum gstPercentage;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalAmount;
    private PaymentModeEnum paymentMode;
    private String referenceNumber;
    private String receiptUrl;
    private String notes;
    private RecordStatusEnum status;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Integer updatedById;
    private String updatedByName;
    private LocalDateTime updatedAt;

    public ExpenseResponseDTO() {}

    public ExpenseResponseDTO(Integer expenseId, String title, Integer categoryId, String categoryName,
                               LocalDate expenseDate, BigDecimal amount, boolean isGstApplicable,
                               GstPercentageEnum gstPercentage, BigDecimal cgstAmount, BigDecimal sgstAmount,
                               BigDecimal igstAmount, BigDecimal totalAmount, PaymentModeEnum paymentMode,
                               String referenceNumber, String receiptUrl, String notes, RecordStatusEnum status,
                               Integer createdById, String createdByName, LocalDateTime createdAt,
                               Integer updatedById, String updatedByName, LocalDateTime updatedAt) {

        this.expenseId = expenseId;
        this.title = title;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.expenseDate = expenseDate;
        this.amount = amount;
        this.isGstApplicable = isGstApplicable;
        this.gstPercentage = gstPercentage;
        this.cgstAmount = cgstAmount;
        this.sgstAmount = sgstAmount;
        this.igstAmount = igstAmount;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
        this.referenceNumber = referenceNumber;
        this.receiptUrl = receiptUrl;
        this.notes = notes;
        this.status = status;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedById = updatedById;
        this.updatedByName = updatedByName;
        this.updatedAt = updatedAt;
    }

    public Integer getExpenseId() { return expenseId; }
    public void setExpenseId(Integer expenseId) { this.expenseId = expenseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isGstApplicable() { return isGstApplicable; }
    public void setGstApplicable(boolean isGstApplicable) { this.isGstApplicable = isGstApplicable; }

    public GstPercentageEnum getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(GstPercentageEnum gstPercentage) { this.gstPercentage = gstPercentage; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentModeEnum getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentModeEnum paymentMode) { this.paymentMode = paymentMode; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }

    public Integer getCreatedById() { return createdById; }
    public void setCreatedById(Integer createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getUpdatedById() { return updatedById; }
    public void setUpdatedById(Integer updatedById) { this.updatedById = updatedById; }

    public String getUpdatedByName() { return updatedByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
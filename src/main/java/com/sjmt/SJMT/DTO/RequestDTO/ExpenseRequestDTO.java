package com.sjmt.SJMT.DTO.RequestDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sjmt.SJMT.Entity.GstPercentageEnum;
import com.sjmt.SJMT.Entity.PaymentModeEnum;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ExpenseRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be under 200 characters")
    private String title;

    @NotNull(message = "Expense category is required")
    private Integer categoryId;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private boolean isGstApplicable = false;

    private GstPercentageEnum gstPercentage;

    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private PaymentModeEnum paymentMode;

    @Size(max = 100)
    private String referenceNumber;

    @Size(max = 500)
    private String receiptUrl;

    @Size(max = 1000)
    private String notes;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

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
}
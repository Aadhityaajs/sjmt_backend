package com.sjmt.SJMT.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Integer expenseId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;   

    @NotNull(message = "Expense category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) 
    private ExpenseCategoryEntity expenseCategory;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_gst_applicable", nullable = false)
    private boolean isGstApplicable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "gst_percentage", nullable = true, length = 20)
    private GstPercentageEnum gstPercentage;

    @Column(name = "cgst_amount", nullable = true, precision = 15, scale = 2)
    private BigDecimal cgstAmount;

    @Column(name = "sgst_amount", nullable = true, precision = 15, scale = 2)
    private BigDecimal sgstAmount;

    @Column(name = "igst_amount", nullable = true, precision = 15, scale = 2)
    private BigDecimal igstAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = true, length = 20)
    private PaymentModeEnum paymentMode;

    @Column(name = "reference_number", nullable = true, length = 100)
    private String referenceNumber;

    @Column(name = "receipt_url", nullable = true, length = 500)
    private String receiptUrl;

    @Column(name = "notes", nullable = true, length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatusEnum status = RecordStatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private UserEntity createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
    
    // Constructors, getters, and setters omitted for brevity

    public ExpenseEntity(BigDecimal amount, BigDecimal cgstAmount, LocalDateTime createdAt, UserEntity createdBy, ExpenseCategoryEntity expenseCategory, LocalDate expenseDate, GstPercentageEnum gstPercentage, BigDecimal igstAmount, String notes, PaymentModeEnum paymentMode, String receiptUrl, String referenceNumber, BigDecimal sgstAmount, String title, BigDecimal totalAmount, LocalDateTime updatedAt, UserEntity updatedBy) {
        this.amount = amount;
        this.cgstAmount = cgstAmount;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.expenseCategory = expenseCategory;
        this.expenseDate = expenseDate;
        this.gstPercentage = gstPercentage;
        this.igstAmount = igstAmount;
        this.notes = notes;
        this.paymentMode = paymentMode;
        this.receiptUrl = receiptUrl;
        this.referenceNumber = referenceNumber;
        this.sgstAmount = sgstAmount;
        this.title = title;
        this.totalAmount = totalAmount;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public ExpenseEntity() {
    }

    public Integer getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ExpenseCategoryEntity getExpenseCategory() {
        return expenseCategory;
    }

    public void setExpenseCategory(ExpenseCategoryEntity expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isGstApplicable() {
        return isGstApplicable;
    }

    public void setGstApplicable(boolean isGstApplicable) {
        this.isGstApplicable = isGstApplicable;
    }

    public GstPercentageEnum getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(GstPercentageEnum gstPercentage) {
        this.gstPercentage = gstPercentage;
    }

    public BigDecimal getCgstAmount() {
        return cgstAmount;
    }

    public void setCgstAmount(BigDecimal cgstAmount) {
        this.cgstAmount = cgstAmount;
    }

    public BigDecimal getSgstAmount() {
        return sgstAmount;
    }

    public void setSgstAmount(BigDecimal sgstAmount) {
        this.sgstAmount = sgstAmount;
    }

    public BigDecimal getIgstAmount() {
        return igstAmount;
    }

    public void setIgstAmount(BigDecimal igstAmount) {
        this.igstAmount = igstAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentModeEnum getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentModeEnum paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public RecordStatusEnum getStatus() {
        return status;
    }

    public void setStatus(RecordStatusEnum status) {
        this.status = status;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
}

// | Field | Type | Notes |
// |-------|------|-------|
// | `expenseId` | Integer, PK | Auto-increment |
// | `title` | String | Short description, e.g. "Office Rent - Feb 2026" |
// | `expenseCategory` | FK → ExpenseCategoryEntity | Category of the expense |
// | `expenseDate` | LocalDate | Date the expense was incurred |
// | `amount` | BigDecimal | Base amount before GST |
// | `isGstApplicable` | boolean | default false |
// | `gstPercentage` | GstPercentageEnum | nullable, snapshot from selection |
// | `cgstAmount` | BigDecimal | nullable, for intra-state |
// | `sgstAmount` | BigDecimal | nullable, for intra-state |
// | `igstAmount` | BigDecimal | nullable, for inter-state |
// | `totalAmount` | BigDecimal | amount + all applicable GST |
// | `paymentMode` | PaymentModeEnum | CASH / CHEQUE / BANK_TRANSFER / UPI / CARD |
// | `referenceNumber` | String | nullable, for cheque no. / UTR / transaction ID |
// | `receiptUrl` | String | nullable, link or path to receipt/proof |
// | `notes` | String | nullable, internal remarks |
// | `status` | RecordStatusEnum | ACTIVE / DELETED (soft delete) |
// | `createdBy` | FK → UserEntity | Audit trail |
// | `createdAt` | LocalDateTime | Auto-generated |
// | `updatedBy` | FK → UserEntity | Audit trail |
// | `updatedAt` | LocalDateTime | Auto-updated |
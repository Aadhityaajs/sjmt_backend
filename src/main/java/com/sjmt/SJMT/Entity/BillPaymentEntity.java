package com.sjmt.SJMT.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BillPaymentEntity — one row per payment event on a CustomerBill.
 * Supports partial payments, multiple payments, and reversals.
 */
@Entity
@Table(name = "bill_payments")
public class BillPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private CustomerBillEntity bill;

    // Positive = payment received. Negative = reversal.
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentModeEnum paymentMode;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    // PAYMENT or REVERSAL
    @Column(name = "entry_type", nullable = false, length = 20)
    private String entryType = "PAYMENT";

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Getters and Setters ──────────────────────────────────────────────────────

    public Integer getId() { return id; }

    public CustomerBillEntity getBill() { return bill; }
    public void setBill(CustomerBillEntity bill) { this.bill = bill; }

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
}

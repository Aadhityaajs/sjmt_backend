package com.sjmt.SJMT.DTO.RequestDTO;

import com.sjmt.SJMT.Entity.PaymentModeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RecordPaymentRequest {

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentModeEnum paymentMode;

    // UTR number, cheque number, transaction ID, etc.
    private String paymentReference;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentModeEnum getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentModeEnum paymentMode) { this.paymentMode = paymentMode; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
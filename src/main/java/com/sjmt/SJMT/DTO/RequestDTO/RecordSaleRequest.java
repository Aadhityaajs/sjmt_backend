package com.sjmt.SJMT.DTO.RequestDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * Request DTO for recording a manual sale / outgoing stock event.
 */
public class RecordSaleRequest {

    @NotNull(message = "Product master ID is required")
    private Integer productMasterId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Selling rate must be greater than 0")
    private BigDecimal sellingRate;

    @NotNull(message = "Sale date is required")
    @PastOrPresent(message = "Sale date cannot be in the future")
    private LocalDate saleDate;

    private String customerName;

    private String notes;

    public RecordSaleRequest() {
    }

    // Getters and Setters
    public Integer getProductMasterId() { return productMasterId; }
    public void setProductMasterId(Integer productMasterId) { this.productMasterId = productMasterId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getSellingRate() { return sellingRate; }
    public void setSellingRate(BigDecimal sellingRate) { this.sellingRate = sellingRate; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

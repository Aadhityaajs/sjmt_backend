package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CustomerBillItemRequest {

    // Optional — if null, item is a free-text custom line (no stock deduction)
    private Integer productMasterId;

    @NotBlank(message = "Item description is required")
    private String description;

    private String hsnCode;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String unit;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be >= 0")
    private BigDecimal unitPrice;

    private BigDecimal discountPct = BigDecimal.ZERO;

    private BigDecimal gstPct = BigDecimal.ZERO;

    // Getters and Setters
    public Integer getProductMasterId() { return productMasterId; }
    public void setProductMasterId(Integer productMasterId) { this.productMasterId = productMasterId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscountPct() { return discountPct; }
    public void setDiscountPct(BigDecimal discountPct) { this.discountPct = discountPct; }

    public BigDecimal getGstPct() { return gstPct; }
    public void setGstPct(BigDecimal gstPct) { this.gstPct = gstPct; }
}
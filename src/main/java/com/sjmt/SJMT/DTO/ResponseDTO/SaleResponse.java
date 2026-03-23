package com.sjmt.SJMT.DTO.ResponseDTO;

import com.sjmt.SJMT.Entity.RecordStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a recorded sale event.
 */
public class SaleResponse {

    private Integer id;
    private Integer productMasterId;
    private String categoryName;
    private String subCategoryName;
    private BigDecimal quantity;
    private String unitOfMeasurementAbbreviation;
    private BigDecimal sellingRate;
    private BigDecimal totalSaleValue;  // quantity * sellingRate
    private LocalDate saleDate;
    private String customerName;
    private String notes;
    private String cancellationReason;
    private String recordedBy;
    private RecordStatusEnum status;
    private LocalDateTime createdAt;

    public SaleResponse() {
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductMasterId() { return productMasterId; }
    public void setProductMasterId(Integer productMasterId) { this.productMasterId = productMasterId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSubCategoryName() { return subCategoryName; }
    public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnitOfMeasurementAbbreviation() { return unitOfMeasurementAbbreviation; }
    public void setUnitOfMeasurementAbbreviation(String unitOfMeasurementAbbreviation) { this.unitOfMeasurementAbbreviation = unitOfMeasurementAbbreviation; }

    public BigDecimal getSellingRate() { return sellingRate; }
    public void setSellingRate(BigDecimal sellingRate) { this.sellingRate = sellingRate; }

    public BigDecimal getTotalSaleValue() { return totalSaleValue; }
    public void setTotalSaleValue(BigDecimal totalSaleValue) { this.totalSaleValue = totalSaleValue; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

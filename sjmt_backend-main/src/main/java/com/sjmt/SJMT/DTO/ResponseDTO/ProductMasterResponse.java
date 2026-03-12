package com.sjmt.SJMT.DTO.ResponseDTO;

import com.sjmt.SJMT.Entity.RecordStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for ProductMaster — the centralized stock ledger entry.
 */
public class ProductMasterResponse {

    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private Integer subCategoryId;
    private String subCategoryName;
    private String hsnCode;
    private Integer unitOfMeasurementId;
    private String unitOfMeasurementName;
    private String unitOfMeasurementAbbreviation;
    private BigDecimal currentStock;
    private BigDecimal averagePurchaseRate;
    private BigDecimal sellingRate;
    private BigDecimal totalStockValue;  // currentStock * averagePurchaseRate
    private RecordStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductMasterResponse() {
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(Integer subCategoryId) { this.subCategoryId = subCategoryId; }

    public String getSubCategoryName() { return subCategoryName; }
    public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public Integer getUnitOfMeasurementId() { return unitOfMeasurementId; }
    public void setUnitOfMeasurementId(Integer unitOfMeasurementId) { this.unitOfMeasurementId = unitOfMeasurementId; }

    public String getUnitOfMeasurementName() { return unitOfMeasurementName; }
    public void setUnitOfMeasurementName(String unitOfMeasurementName) { this.unitOfMeasurementName = unitOfMeasurementName; }

    public String getUnitOfMeasurementAbbreviation() { return unitOfMeasurementAbbreviation; }
    public void setUnitOfMeasurementAbbreviation(String unitOfMeasurementAbbreviation) { this.unitOfMeasurementAbbreviation = unitOfMeasurementAbbreviation; }

    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }

    public BigDecimal getAveragePurchaseRate() { return averagePurchaseRate; }
    public void setAveragePurchaseRate(BigDecimal averagePurchaseRate) { this.averagePurchaseRate = averagePurchaseRate; }

    public BigDecimal getSellingRate() { return sellingRate; }
    public void setSellingRate(BigDecimal sellingRate) { this.sellingRate = sellingRate; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }

    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

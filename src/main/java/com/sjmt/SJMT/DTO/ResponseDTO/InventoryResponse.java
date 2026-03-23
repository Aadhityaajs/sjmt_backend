package com.sjmt.SJMT.DTO.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.GstPercentageEnum;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

/**
 * Inventory Response DTO
 */
public class InventoryResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private Integer subCategoryId;
    private String subCategoryName;
    private Integer productMasterId;
    private String manufacturerName;
    private BigDecimal purchaseRate;
    private BigDecimal sellingRate;
    private BigDecimal quantity;
    private String hsnCode;
    private GstPercentageEnum gstPercentage;
    private Integer gstPercentageValue;
    private Integer unitOfMeasurementId;
    private String unitOfMeasurementName;
    private String unitOfMeasurementAbbreviation;
    private String driverName;
    private String driverNumber;
    private Integer supplierId;
    private String supplierName;
    private String invoicePdfPath;
    private RecordStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public InventoryResponse() {
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(Integer subCategoryId) { this.subCategoryId = subCategoryId; }
    public String getSubCategoryName() { return subCategoryName; }
    public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }
    public Integer getProductMasterId() { return productMasterId; }
    public void setProductMasterId(Integer productMasterId) { this.productMasterId = productMasterId; }
    public String getManufacturerName() { return manufacturerName; }
    public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
    public BigDecimal getPurchaseRate() { return purchaseRate; }
    public void setPurchaseRate(BigDecimal purchaseRate) { this.purchaseRate = purchaseRate; }
    public BigDecimal getSellingRate() { return sellingRate; }
    public void setSellingRate(BigDecimal sellingRate) { this.sellingRate = sellingRate; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }
    public GstPercentageEnum getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(GstPercentageEnum gstPercentage) { this.gstPercentage = gstPercentage; }
    public Integer getGstPercentageValue() { return gstPercentageValue; }
    public void setGstPercentageValue(Integer gstPercentageValue) { this.gstPercentageValue = gstPercentageValue; }
    public Integer getUnitOfMeasurementId() { return unitOfMeasurementId; }
    public void setUnitOfMeasurementId(Integer unitOfMeasurementId) { this.unitOfMeasurementId = unitOfMeasurementId; }
    public String getUnitOfMeasurementName() { return unitOfMeasurementName; }
    public void setUnitOfMeasurementName(String unitOfMeasurementName) { this.unitOfMeasurementName = unitOfMeasurementName; }
    public String getUnitOfMeasurementAbbreviation() { return unitOfMeasurementAbbreviation; }
    public void setUnitOfMeasurementAbbreviation(String unitOfMeasurementAbbreviation) { this.unitOfMeasurementAbbreviation = unitOfMeasurementAbbreviation; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverNumber() { return driverNumber; }
    public void setDriverNumber(String driverNumber) { this.driverNumber = driverNumber; }
    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getInvoicePdfPath() { return invoicePdfPath; }
    public void setInvoicePdfPath(String invoicePdfPath) { this.invoicePdfPath = invoicePdfPath; }
    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
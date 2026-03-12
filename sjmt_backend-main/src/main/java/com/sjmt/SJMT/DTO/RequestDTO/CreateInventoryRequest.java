package com.sjmt.SJMT.DTO.RequestDTO;

import java.math.BigDecimal;

import com.sjmt.SJMT.Entity.GstPercentageEnum;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Create Inventory Request DTO
 */
public class CreateInventoryRequest {
    
    @NotBlank(message = "Inventory name is required")
    @Size(min = 3, max = 200, message = "Inventory name must be between 3 and 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Category ID is required")
    private Integer categoryId;
    
    @NotNull(message = "SubCategory ID is required")
    private Integer subCategoryId;
    
    @NotBlank(message = "Manufacturer name is required")
    @Size(min = 2, max = 100, message = "Manufacturer name must be between 2 and 100 characters")
    private String manufacturerName;
    
    @NotNull(message = "Purchase rate is required")
    @DecimalMin(value = "0.01", message = "Purchase rate must be greater than 0")
    private BigDecimal purchaseRate;
    
    @NotNull(message = "Selling rate is required")
    @DecimalMin(value = "0.01", message = "Selling rate must be greater than 0")
    private BigDecimal sellingRate;
    
    @NotBlank(message = "HSN code is required")
    @Pattern(regexp = "^[0-9]{4}$|^[0-9]{6}$|^[0-9]{8}$", message = "HSN code must be 4, 6, or 8 digits")
    private String hsnCode;
    
    @NotNull(message = "GST percentage is required")
    private GstPercentageEnum gstPercentage;
    
    @NotNull(message = "Unit of measurement ID is required")
    private Integer unitOfMeasurementId;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    private Integer supplierId;
    
    // Constructors
    public CreateInventoryRequest() {
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public Integer getSubCategoryId() {
        return subCategoryId;
    }
    
    public void setSubCategoryId(Integer subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
    
    public String getManufacturerName() {
        return manufacturerName;
    }
    
    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }
    
    public BigDecimal getPurchaseRate() {
        return purchaseRate;
    }
    
    public void setPurchaseRate(BigDecimal purchaseRate) {
        this.purchaseRate = purchaseRate;
    }
    
    public BigDecimal getSellingRate() {
        return sellingRate;
    }
    
    public void setSellingRate(BigDecimal sellingRate) {
        this.sellingRate = sellingRate;
    }
    
    public String getHsnCode() {
        return hsnCode;
    }
    
    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }
    
    public GstPercentageEnum getGstPercentage() {
        return gstPercentage;
    }
    
    public void setGstPercentage(GstPercentageEnum gstPercentage) {
        this.gstPercentage = gstPercentage;
    }
    
    public Integer getUnitOfMeasurementId() {
        return unitOfMeasurementId;
    }
    
    public void setUnitOfMeasurementId(Integer unitOfMeasurementId) {
        this.unitOfMeasurementId = unitOfMeasurementId;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public Integer getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
}
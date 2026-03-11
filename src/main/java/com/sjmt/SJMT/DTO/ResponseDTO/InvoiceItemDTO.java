package com.sjmt.SJMT.DTO.ResponseDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO representing a single BOM (Bill of Materials) item extracted from invoice PDF
 */
public class InvoiceItemDTO {

    @NotBlank(message = "Invoice Number is required")
    private String invoiceNumber;
    
    @NotNull(message = "GST Percentage is required")
    private Integer gstPercentage;
    
    @NotBlank(message = "HSN Code is required")
    private String hsnCode;
    
    @NotBlank(message = "Name/Description of goods is required")
    private String name;
    
    private String description;
    @NotNull(message = "Purchase Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase rate must be > 0")
    private BigDecimal purchaseRate;
    private BigDecimal sellingRate;
    private String status;
    private String driverName;
    private String driverNumber;
    
    @NotBlank(message = "Manufacturer/Seller Name is required")
    private String manufacturerName;
    
    @NotBlank(message = "Unit of Measurement is required")
    private String unitOfMeasurementName;
    
    @NotBlank(message = "Category Name is required")
    private String categoryName;
    
    @NotBlank(message = "Subcategory Name is required")
    private String subcategoryName;
    
    private String length;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be > 0.001")
    private BigDecimal quantity;
    
    private String grade;
    private String size;

    // Constructors
    public InvoiceItemDTO() {
    }

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Integer getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(Integer gstPercentage) {
        this.gstPercentage = gstPercentage;
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(String driverNumber) {
        this.driverNumber = driverNumber;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getUnitOfMeasurementName() {
        return unitOfMeasurementName;
    }

    public void setUnitOfMeasurementName(String unitOfMeasurementName) {
        this.unitOfMeasurementName = unitOfMeasurementName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
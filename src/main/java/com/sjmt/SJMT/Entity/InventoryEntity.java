package com.sjmt.SJMT.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Inventory Entity
 */
@Entity
@Table(name = "inventory")
public class InventoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotBlank(message = "Inventory name is required")
    @Size(min = 3, max = 200, message = "Inventory name must be between 3 and 200 characters")
    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
    
    @NotNull(message = "SubCategory is required")
    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategoryEntity subCategory;
    
    @NotBlank(message = "Manufacturer name is required")
    @Size(min = 2, max = 100, message = "Manufacturer name must be between 2 and 100 characters")
    @Column(name = "manufacturer_name", nullable = false, length = 100)
    private String manufacturerName;
    
    @NotNull(message = "Purchase rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase rate must be greater than 0")
    @Column(name = "purchase_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchaseRate;
    
    @NotNull(message = "Selling rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling rate must be greater than 0")
    @Column(name = "selling_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingRate;
    
    @NotBlank(message = "HSN code is required")
    @Pattern(regexp = "^[0-9]{4}$|^[0-9]{6}$|^[0-9]{8}$", message = "HSN code must be 4, 6, or 8 digits")
    @Column(name = "hsn_code", nullable = false, length = 8)
    private String hsnCode;
    
    @NotNull(message = "GST percentage is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "gst_percentage", nullable = false, length = 10)
    private GstPercentageEnum gstPercentage;
    
    @NotNull(message = "Unit of measurement is required")
    @ManyToOne
    @JoinColumn(name = "unit_of_measurement_id", nullable = false)
    private UnitOfMeasurementEntity unitOfMeasurement;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatusEnum status = RecordStatusEnum.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public InventoryEntity() {
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
    
    public CategoryEntity getCategory() {
        return category;
    }
    
    public void setCategory(CategoryEntity category) {
        this.category = category;
    }
    
    public SubCategoryEntity getSubCategory() {
        return subCategory;
    }
    
    public void setSubCategory(SubCategoryEntity subCategory) {
        this.subCategory = subCategory;
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
    
    public UnitOfMeasurementEntity getUnitOfMeasurement() {
        return unitOfMeasurement;
    }
    
    public void setUnitOfMeasurement(UnitOfMeasurementEntity unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }
    
    public RecordStatusEnum getStatus() {
        return status;
    }
    
    public void setStatus(RecordStatusEnum status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
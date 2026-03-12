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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * ProductMasterEntity — one row per unique product (identified by category + subCategory).
 * Holds the live consolidated stock count regardless of how many suppliers provide it.
 */
@Entity
@Table(name = "product_master", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "sub_category_id"})
})
public class ProductMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategoryEntity subCategory;

    @Column(name = "hsn_code", length = 8)
    private String hsnCode;

    @ManyToOne
    @JoinColumn(name = "unit_of_measurement_id", nullable = true)
    private UnitOfMeasurementEntity unitOfMeasurement;

    // Live stock: increases on purchase (invoice upload), decreases on sale
    @Column(name = "current_stock", nullable = false, precision = 15, scale = 3)
    private BigDecimal currentStock = BigDecimal.ZERO;

    // Weighted average across all purchase bills
    @Column(name = "average_purchase_rate", precision = 10, scale = 2)
    private BigDecimal averagePurchaseRate;

    @Column(name = "selling_rate", precision = 10, scale = 2)
    private BigDecimal sellingRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatusEnum status = RecordStatusEnum.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;


    public ProductMasterEntity() {
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public CategoryEntity getCategory() { return category; }
    public void setCategory(CategoryEntity category) { this.category = category; }

    public SubCategoryEntity getSubCategory() { return subCategory; }
    public void setSubCategory(SubCategoryEntity subCategory) { this.subCategory = subCategory; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public UnitOfMeasurementEntity getUnitOfMeasurement() { return unitOfMeasurement; }
    public void setUnitOfMeasurement(UnitOfMeasurementEntity unitOfMeasurement) { this.unitOfMeasurement = unitOfMeasurement; }

    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }

    public BigDecimal getAveragePurchaseRate() { return averagePurchaseRate; }
    public void setAveragePurchaseRate(BigDecimal averagePurchaseRate) { this.averagePurchaseRate = averagePurchaseRate; }

    public BigDecimal getSellingRate() { return sellingRate; }
    public void setSellingRate(BigDecimal sellingRate) { this.sellingRate = sellingRate; }

    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

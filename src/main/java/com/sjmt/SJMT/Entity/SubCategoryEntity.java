package com.sjmt.SJMT.Entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * SubCategory Entity
 */
@Entity
@Table(name = "sub_categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "category_id"})
})
public class SubCategoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotBlank(message = "SubCategory name is required")
    @Size(min = 3, max = 100, message = "SubCategory name must be between 3 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
    
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
    
    @OneToMany(mappedBy = "subCategory", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<InventoryEntity> inventoryItems;
    
    // Constructors
    public SubCategoryEntity() {
    }
    
    public SubCategoryEntity(String name, String description, CategoryEntity category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = RecordStatusEnum.ACTIVE;
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
    
    public List<InventoryEntity> getInventoryItems() {
        return inventoryItems;
    }
    
    public void setInventoryItems(List<InventoryEntity> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }
}
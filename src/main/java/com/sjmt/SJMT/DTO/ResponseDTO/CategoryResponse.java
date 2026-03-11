package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.RecordStatusEnum;

/**
 * Category Response DTO
 */
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private RecordStatusEnum status;
    private Long subCategoryCount;
    private Long inventoryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CategoryResponse() {
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }
    public Long getSubCategoryCount() { return subCategoryCount; }
    public void setSubCategoryCount(Long subCategoryCount) { this.subCategoryCount = subCategoryCount; }
    public Long getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(Long inventoryCount) { this.inventoryCount = inventoryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.RecordStatusEnum;

public class ExpenseCategoryResponseDTO {

    private Integer categoryId;
    private String name;
    private String description;
    private RecordStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseCategoryResponseDTO() {}

    public ExpenseCategoryResponseDTO(Integer categoryId, String name, String description,
                                       RecordStatusEnum status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
package com.sjmt.SJMT.DTO.ResponseDTO;


import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.RecordStatusEnum;

/**
 * Unit of Measurement Response DTO
 */
public class UnitOfMeasurementResponse {
    private Integer id;
    private String name;
    private String abbreviation;
    private String description;
    private RecordStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public UnitOfMeasurementResponse() {
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAbbreviation() { return abbreviation; }
    public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RecordStatusEnum getStatus() { return status; }
    public void setStatus(RecordStatusEnum status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

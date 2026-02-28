package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create Unit of Measurement Request DTO
 */
public class CreateUnitOfMeasurementRequest {
    
    @NotBlank(message = "Unit name is required")
    @Size(min = 1, max = 50, message = "Unit name must be between 1 and 50 characters")
    private String name;
    
    @NotBlank(message = "Abbreviation is required")
    @Size(min = 1, max = 10, message = "Abbreviation must be between 1 and 10 characters")
    private String abbreviation;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    // Constructors
    public CreateUnitOfMeasurementRequest() {
    }
    
    public CreateUnitOfMeasurementRequest(String name, String abbreviation, String description) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAbbreviation() {
        return abbreviation;
    }
    
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
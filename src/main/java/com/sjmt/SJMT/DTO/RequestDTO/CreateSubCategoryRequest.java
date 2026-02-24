package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Create SubCategory Request DTO
 */
public class CreateSubCategoryRequest {
    
    @NotBlank(message = "SubCategory name is required")
    @Size(min = 3, max = 100, message = "SubCategory name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Category ID is required")
    private Integer categoryId;
    
    // Constructors
    public CreateSubCategoryRequest() {
    }
    
    public CreateSubCategoryRequest(String name, String description, Integer categoryId) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
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
}


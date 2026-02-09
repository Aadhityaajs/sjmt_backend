package com.sjmt.SJMT.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.RequestDTO.CreateSubCategoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateSubCategoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.SubCategoryResponse;
import com.sjmt.SJMT.Entity.CategoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.SubCategoryEntity;
import com.sjmt.SJMT.Repository.CategoryRepository;
import com.sjmt.SJMT.Repository.SubCategoryRepository;

/**
 * SubCategory Service
 */
@Service
public class SubCategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubCategoryService.class);
    
    @Autowired
    private SubCategoryRepository subCategoryRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Create new subcategory
     */
    @Transactional
    public SubCategoryResponse createSubCategory(CreateSubCategoryRequest request) {
        logger.info("Creating new subcategory: {}", request.getName());
        
        // Verify category exists and is ACTIVE
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
        
        if (category.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot create subcategory under a deleted category");
        }
        
        // Check if subcategory name already exists in this category (case-insensitive)
        if (subCategoryRepository.existsByNameIgnoreCaseAndCategoryId(request.getName(), request.getCategoryId())) {
            throw new RuntimeException("SubCategory with name '" + request.getName() + 
                "' already exists in category '" + category.getName() + "'");
        }
        
        SubCategoryEntity subCategory = new SubCategoryEntity();
        subCategory.setName(request.getName());
        subCategory.setDescription(request.getDescription());
        subCategory.setCategory(category);
        subCategory.setStatus(RecordStatusEnum.ACTIVE);
        
        SubCategoryEntity savedSubCategory = subCategoryRepository.save(subCategory);
        logger.info("SubCategory created successfully: {}", savedSubCategory.getName());
        
        return convertToResponse(savedSubCategory);
    }
    
    /**
     * Get all subcategories with optional filters
     */
    @SuppressWarnings({"BoxedValueEquality", "NumberEquality"})
    public List<SubCategoryResponse> getAllSubCategories(String status, String search, Integer categoryId) {
        logger.info("Fetching subcategories - status: {}, search: {}, categoryId: {}", status, search, categoryId);
        
        RecordStatusEnum statusEnum = parseStatus(status);
        List<SubCategoryEntity> subCategories;
        
        if (categoryId != null) {
            // Get subcategories for specific category
            if (statusEnum != null) {
                subCategories = subCategoryRepository.findByCategoryIdAndStatus(categoryId, statusEnum);
            } else {
                List<SubCategoryEntity> retrieve = subCategoryRepository.findAll();
                subCategories = new ArrayList<>();
                for(SubCategoryEntity retrieved : retrieve){
                    if(retrieved.getCategory() != null && retrieved.getCategory().getId().equals(categoryId)){
                        subCategories.add(retrieved);
                    }
                }
            }
        } else if (search != null && !search.trim().isEmpty()) {
            subCategories = subCategoryRepository.searchSubCategories(search.trim(), statusEnum);
        } else if (statusEnum != null) {
            subCategories = subCategoryRepository.findByStatus(statusEnum);
        } else {
            subCategories = subCategoryRepository.findAll();
        }
        
        return subCategories.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get subcategory by ID
     */
    public SubCategoryResponse getSubCategoryById(Integer id) {
        logger.info("Fetching subcategory by ID: {}", id);
        
        SubCategoryEntity subCategory = subCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SubCategory not found with ID: " + id));
        
        return convertToResponse(subCategory);
    }
    
    /**
     * Update subcategory
     */
    @Transactional
    public SubCategoryResponse updateSubCategory(Integer id, UpdateSubCategoryRequest request) {
        logger.info("Updating subcategory ID: {}", id);
        
        SubCategoryEntity subCategory = subCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SubCategory not found with ID: " + id));
        
        // Verify new category exists and is ACTIVE
        CategoryEntity newCategory = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
        
        if (newCategory.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot move subcategory to a deleted category");
        }
        
        // Check if new name already exists in target category (excluding current subcategory)
        boolean nameExists = subCategoryRepository.findByNameIgnoreCaseAndCategoryId(
            request.getName(), request.getCategoryId())
            .filter(sc -> !sc.getId().equals(id))
            .isPresent();
        
        if (nameExists) {
            throw new RuntimeException("SubCategory with name '" + request.getName() + 
                "' already exists in category '" + newCategory.getName() + "'");
        }
        
        subCategory.setName(request.getName());
        subCategory.setDescription(request.getDescription());
        subCategory.setCategory(newCategory);
        
        SubCategoryEntity updatedSubCategory = subCategoryRepository.save(subCategory);
        logger.info("SubCategory updated successfully: {}", updatedSubCategory.getName());
        
        return convertToResponse(updatedSubCategory);
    }
    
    /**
     * Delete subcategory (soft delete)
     * Only allowed if ALL inventory items are DELETED
     */
    @Transactional
    public void deleteSubCategory(Integer id) {
        logger.info("Attempting to delete subcategory ID: {}", id);
        
        SubCategoryEntity subCategory = subCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SubCategory not found with ID: " + id));
        
        // Check if any ACTIVE inventory items exist
        Long activeInventory = subCategoryRepository.countActiveInventoryItems(id, RecordStatusEnum.ACTIVE);
        if (activeInventory > 0) {
            throw new RuntimeException(
                "Cannot delete subcategory. It has " + activeInventory + 
                " active inventory items. Please delete all inventory items first."
            );
        }
        
        // Perform soft delete
        subCategory.setStatus(RecordStatusEnum.DELETED);
        subCategoryRepository.save(subCategory);
        
        logger.info("SubCategory soft deleted successfully: {}", subCategory.getName());
    }
    
    /**
     * Convert entity to response DTO
     */
    private SubCategoryResponse convertToResponse(SubCategoryEntity subCategory) {
        SubCategoryResponse response = new SubCategoryResponse();
        response.setId(subCategory.getId());
        response.setName(subCategory.getName());
        response.setDescription(subCategory.getDescription());
        response.setCategoryId(subCategory.getCategory().getId());
        response.setCategoryName(subCategory.getCategory().getName());
        response.setStatus(subCategory.getStatus());
        response.setCreatedAt(subCategory.getCreatedAt());
        response.setUpdatedAt(subCategory.getUpdatedAt());
        
        // Include inventory count
        Long inventoryCount = subCategoryRepository.countActiveInventoryItems(
            subCategory.getId(), RecordStatusEnum.ACTIVE);
        response.setInventoryCount(inventoryCount);
        
        return response;
    }
    
    /**
     * Parse status string to enum
     */
    private RecordStatusEnum parseStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return RecordStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status + ". Valid values are: ACTIVE, DELETED, ALL");
        }
    }
}
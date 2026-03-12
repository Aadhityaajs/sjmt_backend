package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.sjmt.SJMT.Exception.DuplicateResourceException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.RequestDTO.CreateCategoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateCategoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.CategoryResponse;
import com.sjmt.SJMT.Entity.CategoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Repository.CategoryRepository;
import com.sjmt.SJMT.Repository.ProductMasterRepository;

/**
 * Category Service
 */
@Service
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductMasterRepository productMasterRepository;
    
    /**
     * Create new category
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        logger.info("Creating new category: {}", request.getName());
        
        // Check if category already exists (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
        
        CategoryEntity category = new CategoryEntity();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(RecordStatusEnum.ACTIVE);
        
        CategoryEntity savedCategory = categoryRepository.save(category);
        logger.info("Category created successfully: {}", savedCategory.getName());
        
        return convertToResponse(savedCategory);
    }
    
    /**
     * Get all categories with optional status filter
     */
    public List<CategoryResponse> getAllCategories(String status, String search) {
        logger.info("Fetching categories with status: {} and search: {}", status, search);
        
        RecordStatusEnum statusEnum = parseStatus(status);
        List<CategoryEntity> categories;
        
        if (search != null && !search.trim().isEmpty()) {
            categories = categoryRepository.searchCategories(search.trim(), statusEnum);
        } else if (statusEnum != null) {
            categories = categoryRepository.findByStatus(statusEnum);
        } else {
            categories = categoryRepository.findAll();
        }

        // Fetch counts for ALL categories in one go
        List<Object[]> subCounts = categoryRepository.countActiveSubCategoriesGroupByCategoryId(RecordStatusEnum.ACTIVE);
        List<Object[]> invCounts = categoryRepository.countActiveInventoryItemsGroupByCategoryId(RecordStatusEnum.ACTIVE);

        Map<Integer, Long> subCategoryMap = subCounts.stream()
            .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));

        Map<Integer, Long> inventoryMap = invCounts.stream()
            .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));

        return categories.stream()
            .map(c -> convertToResponseWithCounts(c, 
                subCategoryMap.getOrDefault(c.getId(), 0L), 
                inventoryMap.getOrDefault(c.getId(), 0L)))
            .collect(Collectors.toList());
    }
    
    /**
     * Get category by ID
     */
    public CategoryResponse getCategoryById(Integer id) {
        logger.info("Fetching category by ID: {}", id);
        
        CategoryEntity category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        return convertToResponse(category);
    }
    
    /**
     * Update category
     */
    @Transactional
    public CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request) {
        logger.info("Updating category ID: {}", id);
        
        CategoryEntity category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Check if new name already exists (excluding current category)
        if (!category.getName().equalsIgnoreCase(request.getName()) && 
            categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        CategoryEntity updatedCategory = categoryRepository.save(category);
        logger.info("Category updated successfully: {}", updatedCategory.getName());
        
        return convertToResponse(updatedCategory);
    }
    
    /**
     * Delete category (soft delete)
     * Only allowed if ALL subcategories and inventory are DELETED
     */
    @Transactional
    public void deleteCategory(Integer id) {
        logger.info("Attempting to delete category ID: {}", id);
        
        CategoryEntity category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Check if any ACTIVE subcategories exist
        Long activeSubCategories = categoryRepository.countActiveSubCategories(id, RecordStatusEnum.ACTIVE);
        if (activeSubCategories > 0) {
            throw new RuntimeException(
                "Cannot delete category. It has " + activeSubCategories + 
                " active subcategories. Please delete all subcategories first."
            );
        }
        
        // Check if any ACTIVE inventory items exist
        Long activeInventory = categoryRepository.countActiveInventoryItems(id, RecordStatusEnum.ACTIVE);
        if (activeInventory > 0) {
            throw new RuntimeException(
                "Cannot delete category. It has " + activeInventory + 
                " active inventory items. Please delete all inventory items first."
            );
        }
        
        // L-07: Check if any ACTIVE product masters exist
        long activeProductMasters = productMasterRepository.countByCategoryIdAndStatus(id, RecordStatusEnum.ACTIVE);
        if (activeProductMasters > 0) {
            throw new RuntimeException(
                "Cannot delete category. It is linked to " + activeProductMasters + 
                " active products in the stock ledger."
            );
        }
        
        // Perform soft delete
        category.setStatus(RecordStatusEnum.DELETED);
        categoryRepository.save(category);
        
        logger.info("Category soft deleted successfully: {}", category.getName());
    }
    
    /**
     * Convert entity to response DTO (with lazy loading queries)
     */
    private CategoryResponse convertToResponse(CategoryEntity category) {
        Long subCategoryCount = categoryRepository.countActiveSubCategories(
            category.getId(), RecordStatusEnum.ACTIVE);
        Long inventoryCount = categoryRepository.countActiveInventoryItems(
            category.getId(), RecordStatusEnum.ACTIVE);
        return convertToResponseWithCounts(category, subCategoryCount, inventoryCount);
    }
    
    /**
     * Convert entity to response DTO using pre-fetched counts
     */
    private CategoryResponse convertToResponseWithCounts(CategoryEntity category, Long subCategoryCount, Long inventoryCount) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setStatus(category.getStatus());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        response.setSubCategoryCount(subCategoryCount);
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

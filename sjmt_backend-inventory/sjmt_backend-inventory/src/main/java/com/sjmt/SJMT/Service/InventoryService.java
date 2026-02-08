package com.sjmt.SJMT.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.RequestDTO.CreateInventoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateInventoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.InventoryResponse;
import com.sjmt.SJMT.Entity.CategoryEntity;
import com.sjmt.SJMT.Entity.InventoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.SubCategoryEntity;
import com.sjmt.SJMT.Entity.UnitOfMeasurementEntity;
import com.sjmt.SJMT.Repository.CategoryRepository;
import com.sjmt.SJMT.Repository.InventoryRepository;
import com.sjmt.SJMT.Repository.SubCategoryRepository;
import com.sjmt.SJMT.Repository.UnitOfMeasurementRepository;

/**
 * Inventory Service
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SubCategoryRepository subCategoryRepository;
    
    @Autowired
    private UnitOfMeasurementRepository unitOfMeasurementRepository;
    
    /**
     * Create new inventory item
     */
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        logger.info("Creating new inventory item: {}", request.getName());
        
        // Check if inventory name already exists (case-insensitive)
        if (inventoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Inventory item with name '" + request.getName() + "' already exists");
        }
        
        // Verify category exists and is ACTIVE
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
        
        if (category.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot create inventory item under a deleted category");
        }
        
        // Verify subcategory exists and is ACTIVE
        SubCategoryEntity subCategory = subCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new RuntimeException("SubCategory not found with ID: " + request.getSubCategoryId()));
        
        if (subCategory.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot create inventory item under a deleted subcategory");
        }
        
        // Verify subcategory belongs to specified category
        if (!subCategory.getCategory().getId().equals(request.getCategoryId())) {
            throw new RuntimeException("SubCategory does not belong to the specified category");
        }
        
        // Verify unit of measurement exists and is ACTIVE
        UnitOfMeasurementEntity uom = unitOfMeasurementRepository.findById(request.getUnitOfMeasurementId())
            .orElseThrow(() -> new RuntimeException("Unit of measurement not found with ID: " + request.getUnitOfMeasurementId()));
        
        if (uom.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot use a deleted unit of measurement");
        }
        
        InventoryEntity inventory = new InventoryEntity();
        inventory.setName(request.getName());
        inventory.setDescription(request.getDescription());
        inventory.setCategory(category);
        inventory.setSubCategory(subCategory);
        inventory.setManufacturerName(request.getManufacturerName());
        inventory.setPurchaseRate(request.getPurchaseRate());
        inventory.setSellingRate(request.getSellingRate());
        inventory.setHsnCode(request.getHsnCode());
        inventory.setGstPercentage(request.getGstPercentage());
        inventory.setUnitOfMeasurement(uom);
        inventory.setStatus(RecordStatusEnum.ACTIVE);
        
        InventoryEntity savedInventory = inventoryRepository.save(inventory);
        logger.info("Inventory item created successfully: {}", savedInventory.getName());
        
        return convertToResponse(savedInventory);
    }
    
    /**
     * Get all inventory items with optional filters
     */
    public List<InventoryResponse> getAllInventory(String status, String search, Integer categoryId, Integer subCategoryId) {
        logger.info("Fetching inventory - status: {}, search: {}, categoryId: {}, subCategoryId: {}", 
            status, search, categoryId, subCategoryId);
        
        RecordStatusEnum statusEnum = parseStatus(status);
        List<InventoryEntity> inventory;
        
        if (subCategoryId != null) {
            // Get inventory for specific subcategory
            if (statusEnum != null) {
                inventory = inventoryRepository.findBySubCategoryIdAndStatus(subCategoryId, statusEnum);
            } else {
                inventory = inventoryRepository.findAll().stream()
                    .filter(inv -> inv.getSubCategory().getId().equals(subCategoryId))
                    .collect(Collectors.toList());
            }
        } else if (categoryId != null) {
            // Get inventory for specific category
            if (statusEnum != null) {
                inventory = inventoryRepository.findByCategoryIdAndStatus(categoryId, statusEnum);
            } else {
                inventory = inventoryRepository.findAll().stream()
                    .filter(inv -> inv.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
            }
        } else if (search != null && !search.trim().isEmpty()) {
            inventory = inventoryRepository.searchInventory(search.trim(), statusEnum);
        } else if (statusEnum != null) {
            inventory = inventoryRepository.findByStatus(statusEnum);
        } else {
            inventory = inventoryRepository.findAll();
        }
        
        return inventory.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get inventory item by ID
     */
    public InventoryResponse getInventoryById(Integer id) {
        logger.info("Fetching inventory item by ID: {}", id);
        
        InventoryEntity inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));
        
        return convertToResponse(inventory);
    }
    
    /**
     * Update inventory item
     */
    @Transactional
    public InventoryResponse updateInventory(Integer id, UpdateInventoryRequest request) {
        logger.info("Updating inventory item ID: {}", id);
        
        InventoryEntity inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));
        
        // Check if new name already exists (excluding current inventory)
        if (!inventory.getName().equalsIgnoreCase(request.getName()) && 
            inventoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Inventory item with name '" + request.getName() + "' already exists");
        }
        
        // Verify category exists and is ACTIVE
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
        
        if (category.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot assign inventory to a deleted category");
        }
        
        // Verify subcategory exists and is ACTIVE
        SubCategoryEntity subCategory = subCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new RuntimeException("SubCategory not found with ID: " + request.getSubCategoryId()));
        
        if (subCategory.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot assign inventory to a deleted subcategory");
        }
        
        // Verify subcategory belongs to specified category
        if (!subCategory.getCategory().getId().equals(request.getCategoryId())) {
            throw new RuntimeException("SubCategory does not belong to the specified category");
        }
        
        // Verify unit of measurement exists and is ACTIVE
        UnitOfMeasurementEntity uom = unitOfMeasurementRepository.findById(request.getUnitOfMeasurementId())
            .orElseThrow(() -> new RuntimeException("Unit of measurement not found with ID: " + request.getUnitOfMeasurementId()));
        
        if (uom.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot use a deleted unit of measurement");
        }
        
        inventory.setName(request.getName());
        inventory.setDescription(request.getDescription());
        inventory.setCategory(category);
        inventory.setSubCategory(subCategory);
        inventory.setManufacturerName(request.getManufacturerName());
        inventory.setPurchaseRate(request.getPurchaseRate());
        inventory.setSellingRate(request.getSellingRate());
        inventory.setHsnCode(request.getHsnCode());
        inventory.setGstPercentage(request.getGstPercentage());
        inventory.setUnitOfMeasurement(uom);
        
        InventoryEntity updatedInventory = inventoryRepository.save(inventory);
        logger.info("Inventory item updated successfully: {}", updatedInventory.getName());
        
        return convertToResponse(updatedInventory);
    }
    
    /**
     * Delete inventory item (soft delete)
     */
    @Transactional
    public void deleteInventory(Integer id) {
        logger.info("Attempting to delete inventory item ID: {}", id);
        
        InventoryEntity inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));
        
        // Perform soft delete
        inventory.setStatus(RecordStatusEnum.DELETED);
        inventoryRepository.save(inventory);
        
        logger.info("Inventory item soft deleted successfully: {}", inventory.getName());
    }
    
    /**
     * Convert entity to response DTO
     */
    private InventoryResponse convertToResponse(InventoryEntity inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setName(inventory.getName());
        response.setDescription(inventory.getDescription());
        response.setCategoryId(inventory.getCategory().getId());
        response.setCategoryName(inventory.getCategory().getName());
        response.setSubCategoryId(inventory.getSubCategory().getId());
        response.setSubCategoryName(inventory.getSubCategory().getName());
        response.setManufacturerName(inventory.getManufacturerName());
        response.setPurchaseRate(inventory.getPurchaseRate());
        response.setSellingRate(inventory.getSellingRate());
        response.setHsnCode(inventory.getHsnCode());
        response.setGstPercentage(inventory.getGstPercentage());
        response.setGstPercentageValue(inventory.getGstPercentage().getValue());
        response.setUnitOfMeasurementId(inventory.getUnitOfMeasurement().getId());
        response.setUnitOfMeasurementName(inventory.getUnitOfMeasurement().getName());
        response.setUnitOfMeasurementAbbreviation(inventory.getUnitOfMeasurement().getAbbreviation());
        response.setStatus(inventory.getStatus());
        response.setCreatedAt(inventory.getCreatedAt());
        response.setUpdatedAt(inventory.getUpdatedAt());
        
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
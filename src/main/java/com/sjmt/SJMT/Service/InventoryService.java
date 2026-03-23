package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.ResponseDTO.PaginatedResponse;
import com.sjmt.SJMT.DTO.RequestDTO.CreateInventoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateInventoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.InventoryResponse;
import com.sjmt.SJMT.Entity.CategoryEntity;
import com.sjmt.SJMT.Entity.InventoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.SubCategoryEntity;
import com.sjmt.SJMT.Entity.UnitOfMeasurementEntity;
import com.sjmt.SJMT.Entity.ProductMasterEntity;
import com.sjmt.SJMT.Repository.CategoryRepository;
import com.sjmt.SJMT.Repository.InventoryRepository;
import com.sjmt.SJMT.Repository.SubCategoryRepository;
import com.sjmt.SJMT.Repository.UnitOfMeasurementRepository;
import com.sjmt.SJMT.Repository.SupplierRepository;
import com.sjmt.SJMT.Entity.SupplierEntity;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

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

    @Autowired
    private StockService stockService;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private UserRepository userRepository;

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
    
    /**
     * Create new inventory item
     */
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        logger.info("Creating new inventory item: {}", request.getName());

        // Verify category exists and is ACTIVE
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        
        if (category.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot create inventory item under a deleted category");
        }
        
        // Verify subcategory exists and is ACTIVE
        SubCategoryEntity subCategory = subCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with ID: " + request.getSubCategoryId()));
        
        if (subCategory.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot create inventory item under a deleted subcategory");
        }
        
        // Verify subcategory belongs to specified category
        if (!subCategory.getCategory().getId().equals(request.getCategoryId())) {
            throw new RuntimeException("SubCategory does not belong to the specified category");
        }
        
        // Verify unit of measurement exists and is ACTIVE
        UnitOfMeasurementEntity uom = unitOfMeasurementRepository.findById(request.getUnitOfMeasurementId())
            .orElseThrow(() -> new ResourceNotFoundException("Unit of measurement not found with ID: " + request.getUnitOfMeasurementId()));
        
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
        inventory.setQuantity(request.getQuantity());

        if (request.getSupplierId() != null) {
            SupplierEntity supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));
            inventory.setSupplier(supplier);
        }

        inventory.setStatus(RecordStatusEnum.ACTIVE);
        
        UserEntity currentUser = getCurrentUser();
        inventory.setCreatedBy(currentUser);
        inventory.setUpdatedBy(currentUser);
        
        // M-19: Apply stockService.addStock() upon Manual Inventory Creation
        ProductMasterEntity productMaster = stockService.findOrCreateProductMaster(
                category, subCategory, request.getHsnCode(), uom);
        inventory.setProductMaster(productMaster);

        if (request.getQuantity() != null && request.getQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
            stockService.addStock(productMaster, request.getQuantity(), request.getPurchaseRate());
        }
        
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
                inventory = inventoryRepository.findBySubCategoryId(subCategoryId);
            }
        } else if (categoryId != null) {
            // Get inventory for specific category
            if (statusEnum != null) {
                inventory = inventoryRepository.findByCategoryIdAndStatus(categoryId, statusEnum);
            } else {
                inventory = inventoryRepository.findByCategoryId(categoryId);
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
     * Get all inventory items with optional filters (paginated)
     */
    public PaginatedResponse<InventoryResponse> getAllInventory(String status, String search,
            Integer categoryId, Integer subCategoryId, int page, int size) {
        logger.info("Fetching paginated inventory - status: {}, search: {}, categoryId: {}, subCategoryId: {}, page: {}, size: {}",
            status, search, categoryId, subCategoryId, page, size);

        RecordStatusEnum statusEnum = parseStatus(status);
        PageRequest pageable = PageRequest.of(page, size);
        Page<InventoryEntity> inventoryPage;

        if (subCategoryId != null) {
            if (statusEnum != null) {
                inventoryPage = inventoryRepository.findBySubCategoryIdAndStatus(subCategoryId, statusEnum, pageable);
            } else {
                inventoryPage = inventoryRepository.findBySubCategoryId(subCategoryId, pageable);
            }
        } else if (categoryId != null) {
            if (statusEnum != null) {
                inventoryPage = inventoryRepository.findByCategoryIdAndStatus(categoryId, statusEnum, pageable);
            } else {
                inventoryPage = inventoryRepository.findByCategoryId(categoryId, pageable);
            }
        } else if (search != null && !search.trim().isEmpty()) {
            inventoryPage = inventoryRepository.searchInventory(search.trim(), statusEnum, pageable);
        } else if (statusEnum != null) {
            inventoryPage = inventoryRepository.findByStatus(statusEnum, pageable);
        } else {
            inventoryPage = inventoryRepository.findAll(pageable);
        }

        List<InventoryResponse> content = inventoryPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        return PaginatedResponse.from(inventoryPage, content);
    }
    
    /**
     * Get inventory item by ID
     */
    public InventoryResponse getInventoryById(Integer id) {
        logger.info("Fetching inventory item by ID: {}", id);
        
        InventoryEntity inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + id));
        
        return convertToResponse(inventory);
    }
    
    /**
     * Update inventory item
     */
    @Transactional
    public InventoryResponse updateInventory(Integer id, UpdateInventoryRequest request) {
        logger.info("Updating inventory item ID: {}", id);
        
        InventoryEntity inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + id));
        
        // Verify category exists and is ACTIVE
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        
        if (category.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot assign inventory to a deleted category");
        }
        
        // Verify subcategory exists and is ACTIVE
        SubCategoryEntity subCategory = subCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with ID: " + request.getSubCategoryId()));
        
        if (subCategory.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot assign inventory to a deleted subcategory");
        }
        
        // Verify subcategory belongs to specified category
        if (!subCategory.getCategory().getId().equals(request.getCategoryId())) {
            throw new RuntimeException("SubCategory does not belong to the specified category");
        }
        
        // Verify unit of measurement exists and is ACTIVE
        UnitOfMeasurementEntity uom = unitOfMeasurementRepository.findById(request.getUnitOfMeasurementId())
            .orElseThrow(() -> new ResourceNotFoundException("Unit of measurement not found with ID: " + request.getUnitOfMeasurementId()));
        
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
        inventory.setQuantity(request.getQuantity());
        
        if (request.getSupplierId() != null) {
            SupplierEntity supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));
            inventory.setSupplier(supplier);
        } else {
            inventory.setSupplier(null);
        }
        
        inventory.setUpdatedBy(getCurrentUser());
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + id));

        // Deduct quantity from ProductMaster before soft-deleting this purchase record
        ProductMasterEntity productMaster = inventory.getProductMaster();
        if (productMaster != null && inventory.getQuantity() != null
                && inventory.getQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
            try {
                stockService.deductStock(productMaster, inventory.getQuantity());
                logger.info("Deducted {} from product master ID {} due to inventory delete",
                        inventory.getQuantity(), productMaster.getId());
            } catch (IllegalStateException e) {
                throw new RuntimeException(String.format(
                        "Cannot delete — this purchase record's stock (%s) has already been partially or fully sold.",
                        inventory.getQuantity()
                ));
            }
        }

        // Perform soft delete
        inventory.setStatus(RecordStatusEnum.DELETED);
        inventoryRepository.save(inventory);

        // Recalculate weighted average purchase rate now that this purchase record is removed
        if (productMaster != null) {
            stockService.recalculateAveragePurchaseRate(productMaster);
        }

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
        if (inventory.getCategory() != null) {
            response.setCategoryId(inventory.getCategory().getId());
            response.setCategoryName(inventory.getCategory().getName());
        }
        if (inventory.getSubCategory() != null) {
            response.setSubCategoryId(inventory.getSubCategory().getId());
            response.setSubCategoryName(inventory.getSubCategory().getName());
        }
        if (inventory.getProductMaster() != null) {
            response.setProductMasterId(inventory.getProductMaster().getId());
        }
        response.setManufacturerName(inventory.getManufacturerName());
        response.setPurchaseRate(inventory.getPurchaseRate());
        response.setSellingRate(inventory.getSellingRate());
        response.setQuantity(inventory.getQuantity());
        response.setHsnCode(inventory.getHsnCode());
        response.setGstPercentage(inventory.getGstPercentage());
        if (inventory.getGstPercentage() != null) {
            response.setGstPercentageValue(inventory.getGstPercentage().getValue());
        }
        if (inventory.getUnitOfMeasurement() != null) {
            response.setUnitOfMeasurementId(inventory.getUnitOfMeasurement().getId());
            response.setUnitOfMeasurementName(inventory.getUnitOfMeasurement().getName());
            response.setUnitOfMeasurementAbbreviation(inventory.getUnitOfMeasurement().getAbbreviation());
        }
        
        if (inventory.getSupplier() != null) {
            response.setSupplierId(inventory.getSupplier().getSupplierId());
            response.setSupplierName(inventory.getSupplier().getSupplierName());
        }
        
        response.setDriverName(inventory.getDriverName());
        response.setDriverNumber(inventory.getDriverNumber());
        response.setInvoicePdfPath(inventory.getInvoicePdfPath());
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

    /**
     * Get inventory summary statistics
     */
    public Map<String, Object> getInventoryStats() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        long totalActive  = inventoryRepository.countByStatus(RecordStatusEnum.ACTIVE);
        long totalDeleted = inventoryRepository.countByStatus(RecordStatusEnum.DELETED);
        BigDecimal totalPurchaseValue = inventoryRepository.sumPurchaseRateByStatus(RecordStatusEnum.ACTIVE);
        long totalWithPdf    = inventoryRepository.countItemsWithPdf();
        long addedThisMonth  = inventoryRepository.countByCurrentMonth(year, month);

        return Map.of(
                "totalActiveItems",        totalActive,
                "totalDeletedItems",       totalDeleted,
                "totalPurchaseValue",      totalPurchaseValue,
                "totalItemsWithInvoicePdf", totalWithPdf,
                "itemsAddedThisMonth",     addedThisMonth,
                "reportGeneratedAt",       now.toString()
        );
    }
}

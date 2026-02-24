package com.sjmt.SJMT.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sjmt.SJMT.DTO.RequestDTO.CreateCategoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateCategoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.CategoryResponse;
import com.sjmt.SJMT.Service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Category Controller
 * @author SJMT Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/categories")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Create new category
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Create category", description = "Create new category (CREATE or UPDATE privilege required)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        try {
            logger.info("Create category request: {}", request.getName());
            CategoryResponse response = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
        } catch (Exception e) {
            logger.error("Create category failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get all categories with optional filters
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get all categories", description = "Get all categories with optional status and search filters")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) String search) {
        try {
            logger.info("Get all categories - status: {}, search: {}", status, search);
            List<CategoryResponse> response = categoryService.getAllCategories(status, search);
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Get categories failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get category by ID", description = "Get category details by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Integer id) {
        try {
            logger.info("Get category by ID: {}", id);
            CategoryResponse response = categoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Get category failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Update category", description = "Update category (UPDATE privilege required)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            logger.info("Update category ID: {}", id);
            CategoryResponse response = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
        } catch (Exception e) {
            logger.error("Update category failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Delete category (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Delete category", description = "Soft delete category (UPDATE privilege required). Only allowed if all subcategories and inventory are deleted.")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        try {
            logger.info("Delete category ID: {}", id);
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
        } catch (Exception e) {
            logger.error("Delete category failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
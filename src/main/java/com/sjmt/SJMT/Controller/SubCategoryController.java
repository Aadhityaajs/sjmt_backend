// SubCategoryController.java
package com.sjmt.SJMT.Controller;

import java.util.List;

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

import com.sjmt.SJMT.DTO.RequestDTO.CreateSubCategoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateSubCategoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.SubCategoryResponse;
import com.sjmt.SJMT.Service.SubCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/subcategories")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "SubCategory", description = "SubCategory management APIs")
public class SubCategoryController {
    
    @Autowired
    private SubCategoryService subCategoryService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Create subcategory")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> createSubCategory(@Valid @RequestBody CreateSubCategoryRequest request) {
        try {
            SubCategoryResponse response = subCategoryService.createSubCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("SubCategory created", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get all subcategories")
    public ResponseEntity<ApiResponse<List<SubCategoryResponse>>> getAllSubCategories(
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) @Size(max = 50, message = "Search term cannot exceed 50 characters") String search,
            @RequestParam(required = false) Integer categoryId) {
        try {
            List<SubCategoryResponse> response = subCategoryService.getAllSubCategories(status, search, categoryId);
            return ResponseEntity.ok(ApiResponse.success("SubCategories retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get subcategory by ID")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> getSubCategoryById(@PathVariable Integer id) {
        try {
            SubCategoryResponse response = subCategoryService.getSubCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success("SubCategory retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Update subcategory")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> updateSubCategory(
            @PathVariable Integer id, @Valid @RequestBody UpdateSubCategoryRequest request) {
        try {
            SubCategoryResponse response = subCategoryService.updateSubCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("SubCategory updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Delete subcategory")
    public ResponseEntity<ApiResponse<Void>> deleteSubCategory(@PathVariable Integer id) {
        try {
            subCategoryService.deleteSubCategory(id);
            return ResponseEntity.ok(ApiResponse.success("SubCategory deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

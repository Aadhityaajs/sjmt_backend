// InventoryController.java

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

import com.sjmt.SJMT.DTO.RequestDTO.CreateInventoryRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateInventoryRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.InventoryResponse;
import com.sjmt.SJMT.Service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Inventory", description = "Inventory management APIs")
class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Create inventory item")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        try {
            InventoryResponse response = inventoryService.createInventory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Inventory created", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get all inventory")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory(
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer subCategoryId) {
        try {
            List<InventoryResponse> response = inventoryService.getAllInventory(status, search, categoryId, subCategoryId);
            return ResponseEntity.ok(ApiResponse.success("Inventory retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get inventory by ID")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable Integer id) {
        try {
            InventoryResponse response = inventoryService.getInventoryById(id);
            return ResponseEntity.ok(ApiResponse.success("Inventory retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Update inventory")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable Integer id, @Valid @RequestBody UpdateInventoryRequest request) {
        try {
            InventoryResponse response = inventoryService.updateInventory(id, request);
            return ResponseEntity.ok(ApiResponse.success("Inventory updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Delete inventory")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Integer id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.ok(ApiResponse.success("Inventory deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

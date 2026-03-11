package com.sjmt.SJMT.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sjmt.SJMT.DTO.RequestDTO.SupplierRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.SupplierResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.PaginatedResponse;
import com.sjmt.SJMT.Service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suppliers")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Supplier", description = "Supplier Management APIs")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Get all suppliers (paginated)", description = "Get paginated list of all suppliers")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<PaginatedResponse<SupplierResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved", supplierService.getAllSuppliers(page, size)));
    }

    /**
     * Fetch details of a specific supplier
     * Allowed: READ, CREATE, UPDATE privileges
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Fetch details of a specific supplier using their ID")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Supplier details retrieved successfully", supplierService.getSupplierById(id)));
    }

    @PostMapping
    @Operation(summary = "Create new supplier", description = "Create new supplier")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier created", supplierService.createSupplier(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier details", description = "Update supplier details (Admin only)")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(@PathVariable Integer id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier updated", supplierService.updateSupplier(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier", description = "Soft delete supplier (Admin only)")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        supplierService.softDeleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier blacklisted (soft-deleted)"));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle supplier status", description = "Switch between WHITELISTED and BLACKLISTED")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> toggleStatus(@PathVariable Integer id) {
        SupplierResponse response = supplierService.toggleSupplierStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier status toggled successfully", response));
    }
}
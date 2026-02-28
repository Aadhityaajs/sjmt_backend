// UnitOfMeasurementController.java


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

import com.sjmt.SJMT.DTO.RequestDTO.CreateUnitOfMeasurementRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateUnitOfMeasurementRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.UnitOfMeasurementResponse;
import com.sjmt.SJMT.Service.UnitOfMeasurementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/units")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Unit of Measurement", description = "Unit of Measurement management APIs")
class UnitOfMeasurementController {
    
    @Autowired
    private UnitOfMeasurementService unitService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Create unit")
    public ResponseEntity<ApiResponse<UnitOfMeasurementResponse>> createUnit(@Valid @RequestBody CreateUnitOfMeasurementRequest request) {
        try {
            UnitOfMeasurementResponse response = unitService.createUnit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Unit created", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get all units")
    public ResponseEntity<ApiResponse<List<UnitOfMeasurementResponse>>> getAllUnits(
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) String search) {
        try {
            List<UnitOfMeasurementResponse> response = unitService.getAllUnits(status, search);
            return ResponseEntity.ok(ApiResponse.success("Units retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    @Operation(summary = "Get unit by ID")
    public ResponseEntity<ApiResponse<UnitOfMeasurementResponse>> getUnitById(@PathVariable Integer id) {
        try {
            UnitOfMeasurementResponse response = unitService.getUnitById(id);
            return ResponseEntity.ok(ApiResponse.success("Unit retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Update unit")
    public ResponseEntity<ApiResponse<UnitOfMeasurementResponse>> updateUnit(
            @PathVariable Integer id, @Valid @RequestBody UpdateUnitOfMeasurementRequest request) {
        try {
            UnitOfMeasurementResponse response = unitService.updateUnit(id, request);
            return ResponseEntity.ok(ApiResponse.success("Unit updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    @Operation(summary = "Delete unit")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable Integer id) {
        try {
            unitService.deleteUnit(id);
            return ResponseEntity.ok(ApiResponse.success("Unit deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
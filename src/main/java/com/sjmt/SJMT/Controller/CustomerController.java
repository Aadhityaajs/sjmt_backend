package com.sjmt.SJMT.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sjmt.SJMT.DTO.RequestDTO.CustomerRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.CustomerResponse;
import com.sjmt.SJMT.Service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Customer", description = "Customer Management APIs")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved", customerService.getAllCustomers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_READ', 'PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved", customerService.getCustomerById(id)));
    }

    @PostMapping
    @Operation(summary = "Create customer")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer created", customerService.createCustomer(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable Integer id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.updateCustomer(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (Blacklist) customer")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        customerService.softDeleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer blacklisted (soft-deleted)"));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle customer status", description = "Switch between WHITELISTED and BLACKLISTED")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> toggleStatus(@PathVariable Integer id) {
        CustomerResponse response = customerService.toggleCustomerStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Customer status toggled successfully", response));
    }
}
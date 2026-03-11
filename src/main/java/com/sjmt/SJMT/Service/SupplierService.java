package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.sjmt.SJMT.Exception.DuplicateResourceException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.RequestDTO.SupplierRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.SupplierResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.PaginatedResponse;
import com.sjmt.SJMT.Entity.SupplierEntity;
import com.sjmt.SJMT.Entity.SupplierStatusEnum;
import com.sjmt.SJMT.Repository.SupplierRepository;

/**
 * Supplier Service
 * Handles CRUD operations with Soft Delete and Default Status logic
 * @author SJMT Team
 */
@Service
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * Fetch all suppliers
     */
    public List<SupplierResponse> getAllSuppliers() {
        logger.info("Fetching all suppliers");
        return supplierRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch all suppliers (paginated)
     */
    public PaginatedResponse<SupplierResponse> getAllSuppliers(int page, int size) {
        logger.info("Fetching all suppliers (paginated) - page: {}, size: {}", page, size);
        org.springframework.data.domain.Page<SupplierEntity> supplierPage =
                supplierRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
        List<SupplierResponse> content = supplierPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return PaginatedResponse.from(supplierPage, content);
    }

    /**
     * Fetch supplier by ID
     */
    public SupplierResponse getSupplierById(Integer id) {
        logger.info("Fetching supplier with ID: {}", id);
        SupplierEntity supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
        return convertToResponse(supplier);
    }

    /**
     * Create new supplier
     * Requirement: Default status is WHITELISTED
     */
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        logger.info("Creating new supplier: {}", request.getSupplierName());

        if (supplierRepository.existsBySupplierEmail(request.getSupplierEmail())) {
            throw new DuplicateResourceException("Supplier email already exists");
        }

        SupplierEntity supplier = new SupplierEntity();
        mapRequestToEntity(request, supplier);
        
        // Explicitly set default status
        supplier.setStatus(SupplierStatusEnum.WHITELISTED);

        SupplierEntity savedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier created successfully with ID: {}", savedSupplier.getSupplierId());
        
        return convertToResponse(savedSupplier);
    }

    /**
     * Update existing supplier
     */
    @Transactional
    public SupplierResponse updateSupplier(Integer id, SupplierRequest request) {
        logger.info("Updating supplier ID: {}", id);

        SupplierEntity supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        // Check if email is being changed to one that already exists
        if (!supplier.getSupplierEmail().equals(request.getSupplierEmail()) &&
            supplierRepository.existsBySupplierEmail(request.getSupplierEmail())) {
            throw new DuplicateResourceException("Email already exists for another supplier");
        }

        mapRequestToEntity(request, supplier);
        SupplierEntity updatedSupplier = supplierRepository.save(supplier);
        
        return convertToResponse(updatedSupplier);
    }

    /**
     * Soft Delete Supplier
     * Requirement: Set status to BLACKLISTED instead of deleting from DB
     */
    @Transactional
    public void softDeleteSupplier(Integer id) {
        logger.info("Soft-deleting (Blacklisting) supplier ID: {}", id);

        SupplierEntity supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        supplier.setStatus(SupplierStatusEnum.DELETED);
        supplierRepository.save(supplier);
        
        logger.info("Supplier ID: {} has been blacklisted successfully", id);
    }

    /**
     * Toggle Supplier Status between WHITELISTED and BLACKLISTED
     */
    @Transactional
    public SupplierResponse toggleSupplierStatus(Integer id) {
        logger.info("Toggling status for supplier ID: {}", id);
        
        SupplierEntity supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        if (supplier.getStatus() == SupplierStatusEnum.DELETED) {
            throw new RuntimeException("Cannot toggle status of a deleted supplier");
        }

        // Toggle logic
        if (supplier.getStatus() == SupplierStatusEnum.WHITELISTED) {
            supplier.setStatus(SupplierStatusEnum.BLACKLISTED);
        } else {
            supplier.setStatus(SupplierStatusEnum.WHITELISTED);
        }

        SupplierEntity updatedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier ID: {} status changed to: {}", id, updatedSupplier.getStatus());
        
        return convertToResponse(updatedSupplier);
    }

    /**
     * Helper: Convert Entity to Response DTO
     */
    private SupplierResponse convertToResponse(SupplierEntity entity) {
        SupplierResponse response = new SupplierResponse();
        response.setSupplierId(entity.getSupplierId());
        response.setSupplierName(entity.getSupplierName());
        response.setSupplierEmail(entity.getSupplierEmail());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setGstNumber(entity.getGstNumber());
        response.setAddress(entity.getAddress());
        response.setCity(entity.getCity());
        response.setState(entity.getState());
        response.setPincode(entity.getPincode());
        response.setCreatedAt(entity.getCreatedAt());
        response.setStatus(entity.getStatus());
        return response;
    }

    /**
     * Helper: Map Request DTO fields to Entity
     */
    private void mapRequestToEntity(SupplierRequest request, SupplierEntity entity) {
        entity.setSupplierName(request.getSupplierName());
        entity.setSupplierEmail(request.getSupplierEmail());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setGstNumber(request.getGstNumber());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setPincode(request.getPincode());
    }
}

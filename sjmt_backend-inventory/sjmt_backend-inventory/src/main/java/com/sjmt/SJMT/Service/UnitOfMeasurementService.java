package com.sjmt.SJMT.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.RequestDTO.CreateUnitOfMeasurementRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateUnitOfMeasurementRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.UnitOfMeasurementResponse;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.UnitOfMeasurementEntity;
import com.sjmt.SJMT.Repository.UnitOfMeasurementRepository;

/**
 * Unit of Measurement Service
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class UnitOfMeasurementService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasurementService.class);
    
    @Autowired
    private UnitOfMeasurementRepository unitOfMeasurementRepository;
    
    /**
     * Create new unit of measurement
     */
    @Transactional
    public UnitOfMeasurementResponse createUnit(CreateUnitOfMeasurementRequest request) {
        logger.info("Creating new unit of measurement: {}", request.getName());
        
        // Check if unit name already exists (case-insensitive)
        if (unitOfMeasurementRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Unit of measurement with name '" + request.getName() + "' already exists");
        }
        
        UnitOfMeasurementEntity unit = new UnitOfMeasurementEntity();
        unit.setName(request.getName());
        unit.setAbbreviation(request.getAbbreviation());
        unit.setDescription(request.getDescription());
        unit.setStatus(RecordStatusEnum.ACTIVE);
        
        UnitOfMeasurementEntity savedUnit = unitOfMeasurementRepository.save(unit);
        logger.info("Unit of measurement created successfully: {}", savedUnit.getName());
        
        return convertToResponse(savedUnit);
    }
    
    /**
     * Get all units with optional filters
     */
    public List<UnitOfMeasurementResponse> getAllUnits(String status, String search) {
        logger.info("Fetching units - status: {}, search: {}", status, search);
        
        RecordStatusEnum statusEnum = parseStatus(status);
        List<UnitOfMeasurementEntity> units;
        
        if (search != null && !search.trim().isEmpty()) {
            units = unitOfMeasurementRepository.searchUnits(search.trim(), statusEnum);
        } else if (statusEnum != null) {
            units = unitOfMeasurementRepository.findByStatus(statusEnum);
        } else {
            units = unitOfMeasurementRepository.findAll();
        }
        
        return units.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unit by ID
     */
    public UnitOfMeasurementResponse getUnitById(Integer id) {
        logger.info("Fetching unit by ID: {}", id);
        
        UnitOfMeasurementEntity unit = unitOfMeasurementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Unit of measurement not found with ID: " + id));
        
        return convertToResponse(unit);
    }
    
    /**
     * Update unit of measurement
     */
    @Transactional
    public UnitOfMeasurementResponse updateUnit(Integer id, UpdateUnitOfMeasurementRequest request) {
        logger.info("Updating unit of measurement ID: {}", id);
        
        UnitOfMeasurementEntity unit = unitOfMeasurementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Unit of measurement not found with ID: " + id));
        
        // Check if new name already exists (excluding current unit)
        if (!unit.getName().equalsIgnoreCase(request.getName()) && 
            unitOfMeasurementRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Unit of measurement with name '" + request.getName() + "' already exists");
        }
        
        unit.setName(request.getName());
        unit.setAbbreviation(request.getAbbreviation());
        unit.setDescription(request.getDescription());
        
        UnitOfMeasurementEntity updatedUnit = unitOfMeasurementRepository.save(unit);
        logger.info("Unit of measurement updated successfully: {}", updatedUnit.getName());
        
        return convertToResponse(updatedUnit);
    }
    
    /**
     * Delete unit of measurement (soft delete)
     */
    @Transactional
    public void deleteUnit(Integer id) {
        logger.info("Attempting to delete unit of measurement ID: {}", id);
        
        UnitOfMeasurementEntity unit = unitOfMeasurementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Unit of measurement not found with ID: " + id));
        
        // Perform soft delete
        unit.setStatus(RecordStatusEnum.DELETED);
        unitOfMeasurementRepository.save(unit);
        
        logger.info("Unit of measurement soft deleted successfully: {}", unit.getName());
    }
    
    /**
     * Convert entity to response DTO
     */
    private UnitOfMeasurementResponse convertToResponse(UnitOfMeasurementEntity unit) {
        UnitOfMeasurementResponse response = new UnitOfMeasurementResponse();
        response.setId(unit.getId());
        response.setName(unit.getName());
        response.setAbbreviation(unit.getAbbreviation());
        response.setDescription(unit.getDescription());
        response.setStatus(unit.getStatus());
        response.setCreatedAt(unit.getCreatedAt());
        response.setUpdatedAt(unit.getUpdatedAt());
        
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
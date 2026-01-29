package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.UnitOfMeasurementEntity;

/**
 * Unit of Measurement Repository
 */
@Repository
public interface UnitOfMeasurementRepository extends JpaRepository<UnitOfMeasurementEntity, Integer> {
    
    List<UnitOfMeasurementEntity> findByStatus(RecordStatusEnum status);
    
    @Query("SELECT u FROM UnitOfMeasurementEntity u WHERE " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.abbreviation) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<UnitOfMeasurementEntity> searchUnits(String search, RecordStatusEnum status);
    
    Boolean existsByNameIgnoreCase(String name);
    
    Optional<UnitOfMeasurementEntity> findByNameIgnoreCase(String name);
}
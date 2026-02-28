package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.InventoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

/**
 * Inventory Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Integer> {
    
    List<InventoryEntity> findByStatus(RecordStatusEnum status);
    
    List<InventoryEntity> findByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status);
    
    List<InventoryEntity> findBySubCategoryIdAndStatus(Integer subCategoryId, RecordStatusEnum status);
    
    @Query("SELECT i FROM InventoryEntity i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.manufacturerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.hsnCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.category.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.subCategory.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<InventoryEntity> searchInventory(String search, RecordStatusEnum status);
    
    Boolean existsByNameIgnoreCase(String name);
    
    Optional<InventoryEntity> findByNameIgnoreCase(String name);
}
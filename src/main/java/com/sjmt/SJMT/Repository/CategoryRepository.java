package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.CategoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

/**
 * Category Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    
    List<CategoryEntity> findByStatus(RecordStatusEnum status);
    
    @Query("SELECT c FROM CategoryEntity c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<CategoryEntity> searchCategories(String search, RecordStatusEnum status);
    
    Boolean existsByNameIgnoreCase(String name);
    
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
    
    @Query("SELECT COUNT(s) FROM SubCategoryEntity s WHERE s.category.id = :categoryId AND s.status = :status")
    Long countActiveSubCategories(Integer categoryId, RecordStatusEnum status);
    
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.category.id = :categoryId AND i.status = :status")
    Long countActiveInventoryItems(Integer categoryId, RecordStatusEnum status);
}
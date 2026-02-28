package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.SubCategoryEntity;

/**
 * SubCategory Repository
 */
@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategoryEntity, Integer> {
    
    List<SubCategoryEntity> findByStatus(RecordStatusEnum status);
    
    List<SubCategoryEntity> findByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status);
    
    @Query("SELECT s FROM SubCategoryEntity s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.category.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<SubCategoryEntity> searchSubCategories(String search, RecordStatusEnum status);
    
    Boolean existsByNameIgnoreCaseAndCategoryId(String name, Integer categoryId);
    
    Optional<SubCategoryEntity> findByNameIgnoreCaseAndCategoryId(String name, Integer categoryId);
    
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.subCategory.id = :subCategoryId AND i.status = :status")
    Long countActiveInventoryItems(Integer subCategoryId, RecordStatusEnum status);
}
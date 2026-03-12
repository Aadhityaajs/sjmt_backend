package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.ProductMasterEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMasterRepository extends JpaRepository<ProductMasterEntity, Integer> {

    // Find by category+subCategory (the unique product identity)
    Optional<ProductMasterEntity> findByCategoryIdAndSubCategoryId(Integer categoryId, Integer subCategoryId);

    // All products with optional status filter
    List<ProductMasterEntity> findByStatus(RecordStatusEnum status);

    // Filter by category
    List<ProductMasterEntity> findByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status);

    long countByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status);
    long countBySubCategoryIdAndStatus(Integer subCategoryId, RecordStatusEnum status);

    // Search by category name or subcategory name
    @Query("SELECT p FROM ProductMasterEntity p " +
           "WHERE p.status = :status " +
           "AND (LOWER(p.category.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.subCategory.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.hsnCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ProductMasterEntity> searchProducts(@Param("search") String search,
                                             @Param("status") RecordStatusEnum status);

    // Low stock alert — products where currentStock <= threshold
    @Query("SELECT p FROM ProductMasterEntity p WHERE p.status = :status AND p.currentStock <= :threshold")
    List<ProductMasterEntity> findLowStockProducts(@Param("threshold") java.math.BigDecimal threshold,
                                                    @Param("status") RecordStatusEnum status);
}

package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.ProductMasterEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMasterRepository extends JpaRepository<ProductMasterEntity, Integer> {

    // Find by category+subCategory (the unique product identity)
    Optional<ProductMasterEntity> findByCategoryIdAndSubCategoryId(Integer categoryId, Integer subCategoryId);

    // Pessimistic write lock — use this whenever stock is about to be deducted
    // so that concurrent bill issuances on the same product serialize correctly.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductMasterEntity p WHERE p.id = :id")
    Optional<ProductMasterEntity> findByIdForUpdate(@Param("id") Integer id);

    // All products with optional status filter
    List<ProductMasterEntity> findByStatus(RecordStatusEnum status);

    // Filter by category
    List<ProductMasterEntity> findByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status);

    long countByStatus(RecordStatusEnum status);
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

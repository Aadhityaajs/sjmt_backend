package com.sjmt.SJMT.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.InventoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

/**
 * Inventory Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Integer> {

    // ── EXISTING QUERIES (unchanged) ────────────────────────────────────────────

    List<InventoryEntity> findByStatus(RecordStatusEnum status);

    List<InventoryEntity> findByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status);

    List<InventoryEntity> findBySubCategoryIdAndStatus(Integer subCategoryId, RecordStatusEnum status);

    List<InventoryEntity> findByCategoryId(Integer categoryId);

    List<InventoryEntity> findBySubCategoryId(Integer subCategoryId);

    @Query("SELECT i FROM InventoryEntity i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.manufacturerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.hsnCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.category.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.subCategory.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<InventoryEntity> searchInventory(String search, RecordStatusEnum status);

    // ── PAGINATED OVERLOADS ─────────────────────────────────────────────────────

    Page<InventoryEntity> findByStatus(RecordStatusEnum status, Pageable pageable);

    Page<InventoryEntity> findByCategoryIdAndStatus(Integer categoryId, RecordStatusEnum status, Pageable pageable);

    Page<InventoryEntity> findBySubCategoryIdAndStatus(Integer subCategoryId, RecordStatusEnum status, Pageable pageable);

    Page<InventoryEntity> findByCategoryId(Integer categoryId, Pageable pageable);

    Page<InventoryEntity> findBySubCategoryId(Integer subCategoryId, Pageable pageable);

    @Query("SELECT i FROM InventoryEntity i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.manufacturerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.hsnCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.category.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.subCategory.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<InventoryEntity> searchInventory(String search, RecordStatusEnum status, Pageable pageable);

    // ── OTHER QUERIES (unchanged) ───────────────────────────────────────────────

    Boolean existsByNameIgnoreCase(String name);

    Optional<InventoryEntity> findByNameIgnoreCase(String name);

    /**
     * Find all inventory items that have a PDF linked (invoicePdfPath is not null)
     * and were created within a specific month and year.
     * Used for: GET /api/invoice/pdf/month/{year}/{month}
     */
    @Query("SELECT i FROM InventoryEntity i WHERE " +
            "i.invoicePdfPath IS NOT NULL AND " +
            "YEAR(i.createdAt) = :year AND " +
            "MONTH(i.createdAt) = :month")
    List<InventoryEntity> findByInvoicePdfMonthAndYear(@Param("year") int year, @Param("month") int month);

    /**
     * Find all inventory items that have a PDF linked and were created
     * between fromDate and toDate (inclusive).
     * Used for: GET /api/invoice/pdf/range?from=&to=
     */
    @Query("SELECT i FROM InventoryEntity i WHERE " +
            "i.invoicePdfPath IS NOT NULL AND " +
            "i.createdAt BETWEEN :from AND :to")
    List<InventoryEntity> findByInvoicePdfDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Find a single inventory item by ID that has a PDF linked.
     * Used for: GET /api/invoice/pdf/{id}
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.id = :id AND i.invoicePdfPath IS NOT NULL")
    Optional<InventoryEntity> findByIdWithPdf(@Param("id") Integer id);

    /**
     * Count total active inventory items.
     * Used for: GET /api/invoice/stats
     */
    long countByStatus(RecordStatusEnum status);

    /**
     * Sum of all purchase rates for active inventory items.
     * Used for: GET /api/invoice/stats
     */
    @Query("SELECT COALESCE(SUM(i.purchaseRate * COALESCE(i.quantity, 1)), 0) FROM InventoryEntity i WHERE i.status = :status")
    java.math.BigDecimal sumPurchaseRateByStatus(@Param("status") RecordStatusEnum status);

    /**
     * Count inventory items that have a PDF linked.
     * Used for: GET /api/invoice/stats
     */
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.invoicePdfPath IS NOT NULL")
    long countItemsWithPdf();

    /**
     * Count inventory items created this month.
     * Used for: GET /api/invoice/stats
     */
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE " +
            "YEAR(i.createdAt) = :year AND MONTH(i.createdAt) = :month")
    long countByCurrentMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Find all purchase (bill) records linked to a given ProductMaster.
     * Used by StockService.getTransactionHistory()
     */
    List<InventoryEntity> findByProductMasterIdAndStatus(Integer productMasterId, RecordStatusEnum status);
}
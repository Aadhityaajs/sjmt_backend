package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<SaleEntity, Integer> {

    // All sales for a given product
    List<SaleEntity> findByProductMasterIdAndStatusOrderByCreatedAtDesc(Integer productMasterId,
                                                                        RecordStatusEnum status);

    // All active sales, newest first
    List<SaleEntity> findByStatusOrderByCreatedAtDesc(RecordStatusEnum status);

    // All active sales, newest first (paginated)
    Page<SaleEntity> findByStatusOrderByCreatedAtDesc(RecordStatusEnum status, Pageable pageable);

    // Sales in a date range for a product
    @Query("SELECT s FROM SaleEntity s WHERE s.productMaster.id = :productMasterId " +
           "AND s.status = :status AND s.saleDate BETWEEN :from AND :to ORDER BY s.saleDate DESC")
    List<SaleEntity> findByProductAndDateRange(@Param("productMasterId") Integer productMasterId,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to,
                                               @Param("status") RecordStatusEnum status);

    // Total quantity sold for a product (for reporting)
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM SaleEntity s " +
           "WHERE s.productMaster.id = :productMasterId AND s.status = :status")
    java.math.BigDecimal sumQuantityByProduct(@Param("productMasterId") Integer productMasterId,
                                              @Param("status") RecordStatusEnum status);

    // Count by status (dashboard)
    long countByStatus(RecordStatusEnum status);

    // Total revenue = SUM(qty * sellingRate) for a given status (dashboard)
    @Query("SELECT COALESCE(SUM(s.quantity * s.sellingRate), 0) FROM SaleEntity s WHERE s.status = :status")
    java.math.BigDecimal sumRevenueByStatus(@Param("status") RecordStatusEnum status);
}

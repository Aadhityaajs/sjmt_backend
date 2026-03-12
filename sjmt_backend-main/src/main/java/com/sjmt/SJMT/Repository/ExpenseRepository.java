package com.sjmt.SJMT.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.ExpenseEntity;
import com.sjmt.SJMT.Entity.PaymentModeEnum;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Integer> {

    Optional<ExpenseEntity> findByExpenseIdAndStatus(Integer expenseId, RecordStatusEnum status);

    // Paginated list with optional filters
    @Query("""
        SELECT e FROM ExpenseEntity e
        WHERE e.status = :status
          AND (:categoryId IS NULL OR e.expenseCategory.categoryId = :categoryId)
          AND (:paymentMode IS NULL OR e.paymentMode = :paymentMode)
          AND (:fromDate IS NULL OR e.expenseDate >= :fromDate)
          AND (:toDate IS NULL OR e.expenseDate <= :toDate)
    """)
    Page<ExpenseEntity> findAllWithFilters(
        @Param("status") RecordStatusEnum status,
        @Param("categoryId") Integer categoryId,
        @Param("paymentMode") PaymentModeEnum paymentMode,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    // Monthly summary grouped by category
    @Query("""
        SELECT e.expenseCategory.name, SUM(e.totalAmount)
        FROM ExpenseEntity e
        WHERE e.status = 'ACTIVE'
          AND YEAR(e.expenseDate) = :year
          AND MONTH(e.expenseDate) = :month
        GROUP BY e.expenseCategory.name
    """)
    List<Object[]> monthlySummaryByCategory(@Param("year") int year, @Param("month") int month);

    // Date range report grouped by category
    @Query("""
        SELECT e.expenseCategory.name, SUM(e.totalAmount)
        FROM ExpenseEntity e
        WHERE e.status = 'ACTIVE'
          AND e.expenseDate BETWEEN :fromDate AND :toDate
        GROUP BY e.expenseCategory.name
    """)
    List<Object[]> dateRangeReportByCategory(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    // GST input credit report
    @Query("""
        SELECT
            SUM(e.cgstAmount),
            SUM(e.sgstAmount),
            SUM(e.igstAmount)
        FROM ExpenseEntity e
        WHERE e.status = 'ACTIVE'
          AND e.isGstApplicable = true
          AND e.expenseDate BETWEEN :fromDate AND :toDate
    """)
    Object[] gstInputCreditReport(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    // Payment mode breakdown
    @Query("""
        SELECT e.paymentMode, SUM(e.totalAmount)
        FROM ExpenseEntity e
        WHERE e.status = 'ACTIVE'
          AND (:fromDate IS NULL OR e.expenseDate >= :fromDate)
          AND (:toDate IS NULL OR e.expenseDate <= :toDate)
        GROUP BY e.paymentMode
    """)
    List<Object[]> paymentModeBreakdown(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.QuotationEntity;
import com.sjmt.SJMT.Entity.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<QuotationEntity, Integer> {

    // For number generation — count quotations starting with the current year
    long countByQuotationNumberStartingWith(String year);

    // Paginated list with optional filters
    Page<QuotationEntity> findByStatus(QuotationStatus status, Pageable pageable);

    Page<QuotationEntity> findByCustomer_CustomerId(Integer customerId, Pageable pageable);

    Page<QuotationEntity> findByStatusAndCustomer_CustomerId(QuotationStatus status, Integer customerId, Pageable pageable);

    // All open (non-terminal) quotations for a customer — used when blacklisting
    List<QuotationEntity> findByCustomer_CustomerIdAndStatusIn(Integer customerId, List<QuotationStatus> statuses);

    // Quotations expiring between two dates (for pre-expiry reminders)
    @Query("SELECT q FROM QuotationEntity q WHERE q.status IN ('DRAFT', 'SENT', 'ACCEPTED') " +
           "AND q.validUntil >= :from AND q.validUntil <= :to")
    List<QuotationEntity> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
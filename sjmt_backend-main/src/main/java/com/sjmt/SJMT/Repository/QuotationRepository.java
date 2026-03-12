package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.QuotationEntity;
import com.sjmt.SJMT.Entity.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationRepository extends JpaRepository<QuotationEntity, Integer> {

    // For number generation — count quotations starting with the current year
    long countByQuotationNumberStartingWith(String year);

    // Paginated list with optional filters
    Page<QuotationEntity> findByStatus(QuotationStatus status, Pageable pageable);

    Page<QuotationEntity> findByCustomer_CustomerId(Integer customerId, Pageable pageable);

    Page<QuotationEntity> findByStatusAndCustomer_CustomerId(QuotationStatus status, Integer customerId, Pageable pageable);
}
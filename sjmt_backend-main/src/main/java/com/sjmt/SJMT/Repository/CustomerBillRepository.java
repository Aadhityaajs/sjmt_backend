package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.BillStatus;
import com.sjmt.SJMT.Entity.CustomerBillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerBillRepository extends JpaRepository<CustomerBillEntity, Integer> {

    // For number generation — count bills starting with the current year
    long countByBillNumberStartingWith(String year);

    // Paginated list with optional filters
    Page<CustomerBillEntity> findByStatus(BillStatus status, Pageable pageable);

    Page<CustomerBillEntity> findByCustomer_CustomerId(Integer customerId, Pageable pageable);

    Page<CustomerBillEntity> findByStatusAndCustomer_CustomerId(BillStatus status, Integer customerId, Pageable pageable);

    // For auto-flagging overdue bills (scheduler use)
    @Query("SELECT b FROM CustomerBillEntity b WHERE b.status IN ('ISSUED', 'PARTIALLY_PAID') AND b.dueDate < :today")
    List<CustomerBillEntity> findOverdueBills(@Param("today") LocalDate today);
}
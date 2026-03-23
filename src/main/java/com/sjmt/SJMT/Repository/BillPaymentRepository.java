package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.BillPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPaymentEntity, Integer> {

    List<BillPaymentEntity> findByBillIdOrderByCreatedAtAsc(Integer billId);
}

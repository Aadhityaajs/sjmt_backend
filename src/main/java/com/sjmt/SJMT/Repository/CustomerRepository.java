package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.CustomerEntity;
import com.sjmt.SJMT.Entity.CustomerStatusEnum;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {
    Optional<CustomerEntity> findByCustomerEmail(String email);
    Boolean existsByCustomerEmail(String email);
    Boolean existsByCustomerName(String name);

    // MEDIUM-11: Exclude DELETED customers from standard list views
    List<CustomerEntity> findByStatusNot(CustomerStatusEnum status);
    Page<CustomerEntity> findByStatusNot(CustomerStatusEnum status, Pageable pageable);
}
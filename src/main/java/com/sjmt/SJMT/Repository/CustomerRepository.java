package com.sjmt.SJMT.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.CustomerEntity;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {
    Optional<CustomerEntity> findByCustomerEmail(String email);
    Boolean existsByCustomerEmail(String email);
    Boolean existsByCustomerName(String name);
}
package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Integer> {
    Optional<SupplierEntity> findBySupplierEmail(String email);
    Boolean existsBySupplierEmail(String email);
    Boolean existsBySupplierName(String name);
}
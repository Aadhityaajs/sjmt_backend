package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.ExpenseCategoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity, Integer> {

    List<ExpenseCategoryEntity> findByStatus(RecordStatusEnum status);

    Optional<ExpenseCategoryEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndStatusNot(String name, RecordStatusEnum status);
}
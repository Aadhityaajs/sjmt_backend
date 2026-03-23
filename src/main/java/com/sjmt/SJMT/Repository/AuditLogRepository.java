package com.sjmt.SJMT.Repository;

import com.sjmt.SJMT.Entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Integer> {

    Page<AuditLogEntity> findByEntityNameAndEntityIdOrderByTimestampDesc(
            String entityName, String entityId, Pageable pageable);

    Page<AuditLogEntity> findByEntityNameOrderByTimestampDesc(
            String entityName, Pageable pageable);

    Page<AuditLogEntity> findByPerformedByOrderByTimestampDesc(
            String performedBy, Pageable pageable);

    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);
}

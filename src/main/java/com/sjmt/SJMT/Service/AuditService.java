package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Entity.AuditLogEntity;
import com.sjmt.SJMT.Repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes immutable audit records for sensitive business operations.
 *
 * Uses REQUIRES_NEW so each audit write commits in its own transaction,
 * ensuring the log persists even if the outer transaction later rolls back,
 * and ensuring a failed audit write does NOT roll back the business operation.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Record an audit event.
     *
     * @param entityName  Domain object type, e.g. "CustomerBill"
     * @param entityId    PK of the audited record (as String)
     * @param action      Verb, e.g. "ISSUED", "CANCELLED", "PAYMENT_RECORDED"
     * @param performedBy Username of the actor; null becomes "SYSTEM"
     * @param details     Optional free-form context (amounts, reasons, etc.)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String entityName, String entityId, String action,
                       String performedBy, String details) {
        try {
            auditLogRepository.save(
                    new AuditLogEntity(entityName, entityId, action, performedBy, details));
        } catch (Exception e) {
            // Never let an audit failure propagate into the business transaction
            log.error("[Audit] Failed to write audit record [{} {} {}]: {}",
                    action, entityName, entityId, e.getMessage(), e);
        }
    }
}

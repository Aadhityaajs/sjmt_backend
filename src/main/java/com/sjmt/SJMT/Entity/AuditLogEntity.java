package com.sjmt.SJMT.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable audit trail for sensitive business operations.
 * Uses a plain String for performedBy so the record survives user deletion.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_name, entity_id"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The domain object type being audited, e.g. "CustomerBill", "Sale", "Quotation" */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    /** The PK of the audited record as a string */
    @Column(name = "entity_id", nullable = false, length = 50)
    private String entityId;

    /** The action performed, e.g. "ISSUED", "CANCELLED", "PAYMENT_RECORDED" */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /** Username of the actor; "SYSTEM" for scheduler-driven actions */
    @Column(name = "performed_by", nullable = false, length = 150)
    private String performedBy;

    /** Free-form detail string (amounts, reasons, status transitions, etc.) */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public AuditLogEntity() {}

    public AuditLogEntity(String entityName, String entityId, String action,
                          String performedBy, String details) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.performedBy = performedBy != null ? performedBy : "SYSTEM";
        this.details = details;
    }

    // Getters
    public Integer getId() { return id; }
    public String getEntityName() { return entityName; }
    public String getEntityId() { return entityId; }
    public String getAction() { return action; }
    public String getPerformedBy() { return performedBy; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

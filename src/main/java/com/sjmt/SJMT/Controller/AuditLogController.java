package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.Entity.AuditLogEntity;
import com.sjmt.SJMT.Repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Read-only endpoint for querying the audit log.
 * Restricted to ADMIN role.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** All audit logs (paginated, newest first) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogEntity>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved",
                auditLogRepository.findAllByOrderByTimestampDesc(pageable)));
    }

    /** Audit logs for a specific entity type + ID (e.g. entityName=CustomerBill&entityId=42) */
    @GetMapping("/entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogEntity>>> getByEntity(
            @RequestParam String entityName,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved",
                auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(
                        entityName, entityId, pageable)));
    }

    /** Audit logs performed by a specific user */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogEntity>>> getByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved",
                auditLogRepository.findByPerformedByOrderByTimestampDesc(username, pageable)));
    }
}

package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.RequestDTO.CreateCustomerBillRequest;
import com.sjmt.SJMT.DTO.RequestDTO.RecordPaymentRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.CustomerBillResponse;
import com.sjmt.SJMT.Entity.BillStatus;
import com.sjmt.SJMT.Service.CustomerBillService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/customer-bills")
public class CustomerBillController {

    private final CustomerBillService billService;

    public CustomerBillController(CustomerBillService billService) {
        this.billService = billService;
    }

    // ── POST /api/customer-bills — Create bill (DRAFT) ────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<CustomerBillResponse> createBill(
            @Valid @RequestBody CreateCustomerBillRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billService.createBill(request, principal.getName()));
    }

    // ── POST /api/customer-bills/from-quotation/{quotationId} — Convert quotation ─
    @PostMapping("/from-quotation/{quotationId}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<CustomerBillResponse> convertFromQuotation(
            @PathVariable Integer quotationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billService.convertFromQuotation(quotationId, billDate, dueDate, principal.getName()));
    }

    // ── GET /api/customer-bills — List all (paginated) ────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CustomerBillResponse>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) BillStatus status,
            @RequestParam(required = false) Integer customerId) {
        return ResponseEntity.ok(billService.getAll(page, size, status, customerId));
    }

    // ── GET /api/customer-bills/{id} — Get one ────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerBillResponse> getBill(@PathVariable Integer id) {
        return ResponseEntity.ok(billService.getById(id));
    }

    // ── PUT /api/customer-bills/{id} — Update (DRAFT only) ───────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<CustomerBillResponse> updateBill(
            @PathVariable Integer id,
            @Valid @RequestBody CreateCustomerBillRequest request,
            Principal principal) {
        return ResponseEntity.ok(billService.updateBill(id, request, principal.getName()));
    }

    // ── PATCH /api/customer-bills/{id}/issue — Issue bill → stock deducted ────────
    @PatchMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<CustomerBillResponse> issueBill(
            @PathVariable Integer id,
            Principal principal) {
        return ResponseEntity.ok(billService.issueBill(id, principal.getName()));
    }

    // ── POST /api/customer-bills/{id}/payment — Record payment ────────────────────
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<CustomerBillResponse> recordPayment(
            @PathVariable Integer id,
            @Valid @RequestBody RecordPaymentRequest request,
            Principal principal) {
        return ResponseEntity.ok(billService.recordPayment(id, request, principal.getName()));
    }

    // ── PATCH /api/customer-bills/{id}/cancel — Cancel bill ───────────────────────
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<CustomerBillResponse> cancelBill(
            @PathVariable Integer id,
            Principal principal) {
        return ResponseEntity.ok(billService.cancelBill(id, principal.getName()));
    }

    // ── DELETE /api/customer-bills/{id} — Delete (DRAFT only) ────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBill(@PathVariable Integer id, Principal principal) {
        billService.deleteBill(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
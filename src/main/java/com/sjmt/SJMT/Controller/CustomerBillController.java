package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.RequestDTO.CreateCustomerBillRequest;
import com.sjmt.SJMT.DTO.RequestDTO.RecordPaymentRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.BillPaymentResponse;
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
    public ResponseEntity<ApiResponse<CustomerBillResponse>> createBill(
            @Valid @RequestBody CreateCustomerBillRequest request,
            Principal principal) {
        CustomerBillResponse data = billService.createBill(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bill created successfully", data));
    }

    // ── POST /api/customer-bills/from-quotation/{quotationId} — Convert quotation ─
    @PostMapping("/from-quotation/{quotationId}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> convertFromQuotation(
            @PathVariable Integer quotationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            Principal principal) {
        CustomerBillResponse data = billService.convertFromQuotation(quotationId, billDate, dueDate, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bill created from quotation successfully", data));
    }

    // ── GET /api/customer-bills — List all (paginated) ────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CustomerBillResponse>>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) BillStatus status,
            @RequestParam(required = false) Integer customerId) {
        Page<CustomerBillResponse> data = billService.getAll(page, size, status, customerId);
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved successfully", data));
    }

    // ── GET /api/customer-bills/overdue — List overdue bills ─────────────────────
    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<java.util.List<CustomerBillResponse>>> getOverdueBills() {
        return ResponseEntity.ok(ApiResponse.success("Overdue bills retrieved successfully", billService.getOverdueBills()));
    }

    // ── GET /api/customer-bills/{id} — Get one ────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> getBill(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved successfully", billService.getById(id)));
    }

    // ── PUT /api/customer-bills/{id} — Update (DRAFT only) ───────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> updateBill(
            @PathVariable Integer id,
            @Valid @RequestBody CreateCustomerBillRequest request,
            Principal principal) {
        CustomerBillResponse data = billService.updateBill(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Bill updated successfully", data));
    }

    // ── PATCH /api/customer-bills/{id}/issue — Issue bill → stock deducted ────────
    @PatchMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> issueBill(
            @PathVariable Integer id,
            Principal principal) {
        CustomerBillResponse data = billService.issueBill(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Bill issued successfully — stock deducted", data));
    }

    // ── POST /api/customer-bills/{id}/payment — Record payment ────────────────────
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> recordPayment(
            @PathVariable Integer id,
            @Valid @RequestBody RecordPaymentRequest request,
            Principal principal) {
        CustomerBillResponse data = billService.recordPayment(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", data));
    }

    // ── PATCH /api/customer-bills/{id}/cancel — Cancel bill ───────────────────────
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> cancelBill(
            @PathVariable Integer id,
            Principal principal) {
        CustomerBillResponse data = billService.cancelBill(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Bill cancelled successfully", data));
    }

    // ── DELETE /api/customer-bills/{id} — Delete (DRAFT only) ────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBill(@PathVariable Integer id, Principal principal) {
        billService.deleteBill(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Bill deleted successfully"));
    }

    // ── GET /api/customer-bills/{id}/payments — Payment ledger ───────────────────
    @GetMapping("/{id}/payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<java.util.List<BillPaymentResponse>>> getPaymentHistory(
            @PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment history retrieved", billService.getPaymentHistory(id)));
    }

    // ── POST /api/customer-bills/{id}/payments/{paymentId}/reverse — Reverse payment
    @PostMapping("/{id}/payments/{paymentId}/reverse")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> reversePayment(
            @PathVariable Integer id,
            @PathVariable Integer paymentId,
            @RequestBody java.util.Map<String, String> body,
            Principal principal) {
        String reason = body.getOrDefault("reason", "No reason provided");
        CustomerBillResponse data = billService.reversePayment(id, paymentId, reason, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Payment reversed successfully", data));
    }
}
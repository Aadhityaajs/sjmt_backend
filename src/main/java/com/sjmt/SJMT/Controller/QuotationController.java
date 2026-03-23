package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.RequestDTO.CreateQuotationRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.QuotationResponse;
import com.sjmt.SJMT.Entity.QuotationStatus;
import com.sjmt.SJMT.Service.QuotationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    // ── POST /api/quotations — Create quotation (DRAFT) ───────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuotation(
            @Valid @RequestBody CreateQuotationRequest request,
            Principal principal) {
        QuotationResponse data = quotationService.createQuotation(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created successfully", data));
    }

    // ── GET /api/quotations — List all (paginated) ────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<QuotationResponse>>> getAllQuotations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(required = false) Integer customerId) {
        Page<QuotationResponse> data = quotationService.getAll(page, size, status, customerId);
        return ResponseEntity.ok(ApiResponse.success("Quotations retrieved successfully", data));
    }

    // ── GET /api/quotations/{id} — Get one ────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotation(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation retrieved successfully", quotationService.getById(id)));
    }

    // ── PUT /api/quotations/{id} — Update (DRAFT only) ────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotation(
            @PathVariable Integer id,
            @Valid @RequestBody CreateQuotationRequest request,
            Principal principal) {
        QuotationResponse data = quotationService.updateQuotation(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Quotation updated successfully", data));
    }

    // ── PATCH /api/quotations/{id}/status — Change status ─────────────────────────
    // Body: { "status": "SENT" }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationResponse>> changeStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            Principal principal) {
        QuotationStatus newStatus = QuotationStatus.valueOf(body.get("status").toUpperCase());
        QuotationResponse data = quotationService.changeStatus(id, newStatus, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Quotation status updated to " + newStatus, data));
    }

    // ── POST /api/quotations/{id}/convert-to-bill — Convert ACCEPTED → Bill ───────
    // Delegates to CustomerBillService via a dedicated endpoint on the bill controller.
    // Here we just validate the quotation is ACCEPTED and return it.
    // Actual bill creation is done via POST /api/customer-bills/from-quotation/{id}
    @PostMapping("/{id}/convert-to-bill")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationResponse>> validateForConversion(@PathVariable Integer id) {
        // Fetch the quotation — throws if not found or not ACCEPTED
        quotationService.getAcceptedQuotationForConversion(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation is eligible for conversion", quotationService.getById(id)));
    }

    // ── GET /api/quotations/expiring-soon — Quotations expiring within N days ──────
    // Used by the frontend dashboard banner (MEDIUM-3 / LOW-1)
    @GetMapping("/expiring-soon")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<QuotationResponse>>> getExpiringSoon(
            @RequestParam(defaultValue = "3") int days) {
        List<QuotationResponse> data = quotationService.getExpiringSoon(days);
        return ResponseEntity.ok(ApiResponse.success("Expiring quotations retrieved", data));
    }

    // ── DELETE /api/quotations/{id} — Delete (DRAFT only) ─────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuotation(@PathVariable Integer id, Principal principal) {
        quotationService.deleteQuotation(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Quotation deleted successfully"));
    }
}
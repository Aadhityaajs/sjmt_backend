package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.RequestDTO.CreateQuotationRequest;
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
    public ResponseEntity<QuotationResponse> createQuotation(
            @Valid @RequestBody CreateQuotationRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quotationService.createQuotation(request, principal.getName()));
    }

    // ── GET /api/quotations — List all (paginated) ────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<QuotationResponse>> getAllQuotations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(required = false) Integer customerId) {
        return ResponseEntity.ok(quotationService.getAll(page, size, status, customerId));
    }

    // ── GET /api/quotations/{id} — Get one ────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuotationResponse> getQuotation(@PathVariable Integer id) {
        return ResponseEntity.ok(quotationService.getById(id));
    }

    // ── PUT /api/quotations/{id} — Update (DRAFT only) ────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<QuotationResponse> updateQuotation(
            @PathVariable Integer id,
            @Valid @RequestBody CreateQuotationRequest request,
            Principal principal) {
        return ResponseEntity.ok(quotationService.updateQuotation(id, request, principal.getName()));
    }

    // ── PATCH /api/quotations/{id}/status — Change status ─────────────────────────
    // Body: { "status": "SENT" }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<QuotationResponse> changeStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            Principal principal) {
        QuotationStatus newStatus = QuotationStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(quotationService.changeStatus(id, newStatus, principal.getName()));
    }

    // ── POST /api/quotations/{id}/convert-to-bill — Convert ACCEPTED → Bill ───────
    // Delegates to CustomerBillService via a dedicated endpoint on the bill controller.
    // Here we just validate the quotation is ACCEPTED and return it.
    // Actual bill creation is done via POST /api/customer-bills/from-quotation/{id}
    @PostMapping("/{id}/convert-to-bill")
    @PreAuthorize("hasAnyAuthority('PRIVILEGE_CREATE', 'PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<QuotationResponse> validateForConversion(@PathVariable Integer id) {
        // Fetch the quotation — throws if not found or not ACCEPTED
        quotationService.getAcceptedQuotationForConversion(id);
        return ResponseEntity.ok(quotationService.getById(id));
    }

    // ── DELETE /api/quotations/{id} — Delete (DRAFT only) ─────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuotation(@PathVariable Integer id, Principal principal) {
        quotationService.deleteQuotation(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
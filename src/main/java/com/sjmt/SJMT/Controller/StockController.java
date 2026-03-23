package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.RequestDTO.RecordSaleRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ProductMasterResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.SaleResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.StockTransactionResponse;
import com.sjmt.SJMT.Service.SaleService;
import com.sjmt.SJMT.Service.StockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.sjmt.SJMT.DTO.ResponseDTO.PaginatedResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * StockController — centralized stock ledger endpoints.
 *
 * GET  /api/stock                         → all products with current stock
 * GET  /api/stock/{id}                    → single product stock detail
 * GET  /api/stock/{id}/transactions       → full purchase+sale history for a product
 * POST /api/stock/sale                    → record a manual sale (deducts stock)
 * GET  /api/stock/sales                   → all recorded sales
 * GET  /api/stock/sales/product/{id}      → sales for a specific product
 */
@RestController
@Validated
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;
    private final SaleService saleService;

    public StockController(StockService stockService, SaleService saleService) {
        this.stockService = stockService;
        this.saleService = saleService;
    }

    // ── GET all products with current stock ──────────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductMasterResponse>> getAllProducts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Size(max = 50, message = "Search term cannot exceed 50 characters") String search,
            @RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(stockService.getAllProducts(status, search, categoryId));
    }

    // ── GET single product ────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductMasterResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(stockService.getProductById(id));
    }

    // ── GET full transaction history for a product ────────────────────────────────
    @GetMapping("/{id}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockTransactionResponse>> getTransactionHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(stockService.getTransactionHistory(id));
    }

    // ── POST record a sale (deducts stock) ────────────────────────────────────────
    @PostMapping("/sale")
    @PreAuthorize("hasAuthority('PRIVILEGE_CREATE') or hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> recordSale(
            @Valid @RequestBody RecordSaleRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SaleResponse response = saleService.recordSale(request, username);
        return ResponseEntity.ok(response);
    }

    // ── GET all sales (paginated) ──────────────────────────────────────────────────
    @GetMapping("/sales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<SaleResponse>> getAllSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(saleService.getAllSales(page, size));
    }

    // ── GET sales for a specific product ──────────────────────────────────────────
    @GetMapping("/sales/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SaleResponse>> getSalesByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(saleService.getSalesByProduct(productId));
    }

    // ── DELETE (cancel) a sale and restore stock ──────────────────────────────────
    @DeleteMapping("/sales/{saleId}/cancel")
    @PreAuthorize("hasAuthority('PRIVILEGE_CREATE') or hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> cancelSale(
            @PathVariable Integer saleId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String reason = body != null ? body.get("reason") : null;
        SaleResponse response = saleService.cancelSale(saleId, username, isAdmin, reason);
        return ResponseEntity.ok(response);
    }

    // ── PATCH update selling rate for a product ───────────────────────────────────
    @PatchMapping("/{id}/selling-rate")
    @PreAuthorize("hasAuthority('PRIVILEGE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<ProductMasterResponse> updateSellingRate(
            @PathVariable Integer id,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal rate = body.get("sellingRate");
        return ResponseEntity.ok(stockService.updateSellingRate(id, rate));
    }

    // ── GET supplier-level stock breakdown (MEDIUM-4) ─────────────────────────────
    @GetMapping("/supplier-breakdown")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getSupplierBreakdown() {
        return ResponseEntity.ok(stockService.getSupplierStockBreakdown());
    }

    // ── POST backfill existing inventory into ProductMaster (Admin only) ──────────
    @PostMapping("/backfill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> backfillFromInventory() {
        return ResponseEntity.ok(stockService.backfillFromInventory());
    }

    // ── Simple error response for insufficient stock ──────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientStock(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}

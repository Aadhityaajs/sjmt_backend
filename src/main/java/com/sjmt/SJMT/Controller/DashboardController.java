package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.Entity.BillStatus;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Repository.CustomerBillRepository;
import com.sjmt.SJMT.Repository.ExpenseRepository;
import com.sjmt.SJMT.Repository.ProductMasterRepository;
import com.sjmt.SJMT.Repository.SaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DashboardController — single aggregated stats endpoint.
 * Replaces per-table full fetches that were being done on the frontend.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductMasterRepository productMasterRepository;
    private final SaleRepository saleRepository;
    private final CustomerBillRepository billRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardController(ProductMasterRepository productMasterRepository,
                               SaleRepository saleRepository,
                               CustomerBillRepository billRepository,
                               ExpenseRepository expenseRepository) {
        this.productMasterRepository = productMasterRepository;
        this.saleRepository = saleRepository;
        this.billRepository = billRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        // Active product count and total stock value
        long activeProducts = productMasterRepository.countByStatus(RecordStatusEnum.ACTIVE);
        BigDecimal totalStockValue = productMasterRepository.findByStatus(RecordStatusEnum.ACTIVE)
                .stream()
                .map(p -> {
                    BigDecimal stock = p.getCurrentStock() != null ? p.getCurrentStock() : BigDecimal.ZERO;
                    BigDecimal rate = p.getAveragePurchaseRate() != null ? p.getAveragePurchaseRate() : BigDecimal.ZERO;
                    return stock.multiply(rate);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Sales stats
        long totalSalesCount = saleRepository.countByStatus(RecordStatusEnum.ACTIVE);
        BigDecimal totalRevenue = saleRepository.sumRevenueByStatus(RecordStatusEnum.ACTIVE);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Overdue bills count
        long overdueBillsCount = billRepository.countByStatus(BillStatus.OVERDUE);

        // Expense count
        long totalExpenseCount = expenseRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeProducts", activeProducts);
        stats.put("totalStockValue", totalStockValue);
        stats.put("totalSalesCount", totalSalesCount);
        stats.put("totalRevenue", totalRevenue.setScale(2, RoundingMode.HALF_UP));
        stats.put("overdueBillsCount", overdueBillsCount);
        stats.put("totalExpenseCount", totalExpenseCount);

        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", stats));
    }
}

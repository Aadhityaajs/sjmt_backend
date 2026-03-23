package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.sjmt.SJMT.DTO.RequestDTO.RecordSaleRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.SaleResponse;
import com.sjmt.SJMT.Entity.CustomerEntity;
import com.sjmt.SJMT.Entity.ProductMasterEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.SaleEntity;
import com.sjmt.SJMT.Repository.CustomerRepository;
import com.sjmt.SJMT.Repository.ProductMasterRepository;
import com.sjmt.SJMT.Repository.SaleRepository;
import com.sjmt.SJMT.DTO.ResponseDTO.PaginatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private static final Logger log = LoggerFactory.getLogger(SaleService.class);

    private final SaleRepository saleRepository;
    private final ProductMasterRepository productMasterRepository;
    private final CustomerRepository customerRepository;
    private final StockService stockService;
    private final AuditService auditService;

    public SaleService(SaleRepository saleRepository,
                       ProductMasterRepository productMasterRepository,
                       CustomerRepository customerRepository,
                       StockService stockService,
                       AuditService auditService) {
        this.saleRepository = saleRepository;
        this.productMasterRepository = productMasterRepository;
        this.customerRepository = customerRepository;
        this.stockService = stockService;
        this.auditService = auditService;
    }

    // ── Record a sale: validates stock, deducts, saves ────────────────────────────
    // Uses a pessimistic write lock on the product row to prevent concurrent
    // bill issuances from over-deducting stock (race condition fix).
    @Transactional
    public SaleResponse recordSale(RecordSaleRequest request, String recordedByUsername) {
        ProductMasterEntity product = productMasterRepository.findByIdForUpdate(request.getProductMasterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + request.getProductMasterId()));

        if (product.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Cannot record sale for a deleted product.");
        }

        // Stock validation — block if insufficient
        BigDecimal currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : BigDecimal.ZERO;
        String categoryName   = product.getCategory()    != null ? product.getCategory().getName()    : "N/A";
        String subCategoryName = product.getSubCategory() != null ? product.getSubCategory().getName() : "N/A";
        if (currentStock.compareTo(request.getQuantity()) < 0) {
            throw new IllegalStateException(String.format(
                    "Insufficient stock for '%s / %s'. Available: %s, Requested: %s",
                    categoryName, subCategoryName,
                    currentStock.toPlainString(),
                    request.getQuantity().toPlainString()));
        }

        // Deduct stock
        stockService.deductStock(product, request.getQuantity());

        // Snapshot the selling rate at time of sale.
        // If the caller did not supply a rate, fall back to ProductMaster.sellingRate.
        java.math.BigDecimal snapshotRate = request.getSellingRate() != null
                ? request.getSellingRate()
                : product.getSellingRate();

        // Save sale record
        SaleEntity sale = new SaleEntity();
        sale.setProductMaster(product);
        sale.setQuantity(request.getQuantity());
        sale.setSellingRate(snapshotRate);
        sale.setSaleDate(request.getSaleDate());
        sale.setCustomerName(request.getCustomerName());
        sale.setNotes(request.getNotes());
        sale.setRecordedBy(recordedByUsername);
        sale.setStatus(RecordStatusEnum.ACTIVE);

        // Link to CustomerEntity if customerId provided (HIGH-6)
        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId())
                    .ifPresent(sale::setCustomer);
        }

        SaleEntity saved = saleRepository.save(sale);
        log.info("Sale recorded — ID {} | product '{}' | qty {} | by {}",
                saved.getId(), categoryName + "/" + subCategoryName,
                request.getQuantity(), recordedByUsername);
        auditService.record("Sale", String.valueOf(saved.getId()), "SALE_RECORDED",
                recordedByUsername, "Product: " + categoryName + "/" + subCategoryName
                        + ", Qty: " + request.getQuantity()
                        + ", Rate: " + snapshotRate
                        + ", Customer: " + request.getCustomerName());

        return convertToResponse(saved);
    }

    // ── Record a sale entry without touching stock (used when stock was already
    // reserved at quotation acceptance time) ─────────────────────────────────────
    @Transactional
    public SaleResponse recordSaleWithoutStockDeduction(RecordSaleRequest request, String recordedByUsername) {
        ProductMasterEntity product = productMasterRepository.findById(request.getProductMasterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + request.getProductMasterId()));

        SaleEntity sale = new SaleEntity();
        sale.setProductMaster(product);
        sale.setQuantity(request.getQuantity());
        sale.setSellingRate(request.getSellingRate());
        sale.setSaleDate(request.getSaleDate());
        sale.setCustomerName(request.getCustomerName());
        sale.setNotes(request.getNotes());
        sale.setRecordedBy(recordedByUsername);
        sale.setStatus(RecordStatusEnum.ACTIVE);

        SaleEntity saved = saleRepository.save(sale);
        log.info("Sale record created (no stock deduction — was pre-reserved) — ID {} | product ID {} | qty {} | by {}",
                saved.getId(), request.getProductMasterId(), request.getQuantity(), recordedByUsername);
        return convertToResponse(saved);
    }

    // ── Cancel a sale and restore stock ──────────────────────────────────────────
    @Transactional
    public SaleResponse cancelSale(Integer saleId, String cancelledByUsername, boolean isAdmin) {
        return cancelSale(saleId, cancelledByUsername, isAdmin, null);
    }

    @Transactional
    public SaleResponse cancelSale(Integer saleId, String cancelledByUsername, boolean isAdmin, String reason) {
        SaleEntity sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + saleId));

        if (sale.getStatus() == RecordStatusEnum.CANCELLED || sale.getStatus() == RecordStatusEnum.DELETED) {
            throw new RuntimeException("Sale ID " + saleId + " is already cancelled or deleted.");
        }

        // Restore quantity back to ProductMaster stock
        ProductMasterEntity product = sale.getProductMaster();
        if (product != null && sale.getQuantity() != null
                && sale.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            
            if (product.getStatus() != RecordStatusEnum.ACTIVE) {
                throw new RuntimeException("Cannot cancel sale. The associated product is no longer active.");
            }
                
            BigDecimal restored = (product.getCurrentStock() != null ? product.getCurrentStock() : BigDecimal.ZERO)
                    .add(sale.getQuantity());
            product.setCurrentStock(restored);
            productMasterRepository.save(product);
            log.info("Stock restored for product ID {} due to sale cancellation by {}: +{}",
                    product.getId(), cancelledByUsername, sale.getQuantity());
        }

        // Mark the sale record as CANCELLED or DELETED
        sale.setStatus(isAdmin ? RecordStatusEnum.DELETED : RecordStatusEnum.CANCELLED);
        // Store cancellation reason in dedicated field (MEDIUM-5)
        if (reason != null && !reason.isBlank()) {
            sale.setCancellationReason(reason.trim());
        }
        SaleEntity saved = saleRepository.save(sale);

        log.info("Sale ID {} cancelled by {}", saleId, cancelledByUsername);
        auditService.record("Sale", String.valueOf(saleId),
                isAdmin ? "SALE_DELETED" : "SALE_CANCELLED",
                cancelledByUsername, "ProductID: "
                        + (sale.getProductMaster() != null ? sale.getProductMaster().getId() : "?")
                        + ", Qty: " + sale.getQuantity()
                        + (reason != null && !reason.isBlank() ? ", Reason: " + reason : ""));
        return convertToResponse(saved);
    }

    // ── All sales (active) ────────────────────────────────────────────────────────
    public List<SaleResponse> getAllSales() {
        return saleRepository.findByStatusOrderByCreatedAtDesc(RecordStatusEnum.ACTIVE)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // ── All sales (active, paginated) ────────────────────────────────────────────
    public PaginatedResponse<SaleResponse> getAllSales(int page, int size) {
        Page<SaleEntity> salePage = saleRepository.findByStatusOrderByCreatedAtDesc(
                RecordStatusEnum.ACTIVE, PageRequest.of(page, size));
        List<SaleResponse> content = salePage.getContent().stream()
                .map(this::convertToResponse).collect(Collectors.toList());
        return PaginatedResponse.from(salePage, content);
    }

    // ── Sales for a specific product ──────────────────────────────────────────────
    public List<SaleResponse> getSalesByProduct(Integer productMasterId) {
        productMasterRepository.findById(productMasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productMasterId));
        return saleRepository.findByProductMasterIdAndStatusOrderByCreatedAtDesc(
                        productMasterId, RecordStatusEnum.ACTIVE)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // ── Convert entity → response DTO ────────────────────────────────────────────
    private SaleResponse convertToResponse(SaleEntity sale) {
        SaleResponse r = new SaleResponse();
        r.setId(sale.getId());

        if (sale.getProductMaster() != null) {
            r.setProductMasterId(sale.getProductMaster().getId());
            if (sale.getProductMaster().getCategory() != null) {
                r.setCategoryName(sale.getProductMaster().getCategory().getName());
            }
            if (sale.getProductMaster().getSubCategory() != null) {
                r.setSubCategoryName(sale.getProductMaster().getSubCategory().getName());
            }
            if (sale.getProductMaster().getUnitOfMeasurement() != null) {
                r.setUnitOfMeasurementAbbreviation(
                        sale.getProductMaster().getUnitOfMeasurement().getAbbreviation());
            }
        }

        r.setQuantity(sale.getQuantity());
        r.setSellingRate(sale.getSellingRate());

        if (sale.getQuantity() != null && sale.getSellingRate() != null) {
            r.setTotalSaleValue(
                    sale.getQuantity().multiply(sale.getSellingRate()).setScale(2, RoundingMode.HALF_UP));
        }

        r.setSaleDate(sale.getSaleDate());
        r.setCustomerName(sale.getCustomerName());
        r.setNotes(sale.getNotes());
        r.setCancellationReason(sale.getCancellationReason());
        r.setRecordedBy(sale.getRecordedBy());
        r.setStatus(sale.getStatus());
        r.setCreatedAt(sale.getCreatedAt());
        return r;
    }
}

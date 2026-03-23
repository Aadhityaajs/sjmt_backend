package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.sjmt.SJMT.DTO.ResponseDTO.ProductMasterResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.StockTransactionResponse;
import com.sjmt.SJMT.Entity.*;
import com.sjmt.SJMT.Repository.InventoryRepository;
import com.sjmt.SJMT.Repository.ProductMasterRepository;
import com.sjmt.SJMT.Repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final ProductMasterRepository productMasterRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final Lock backfillLock = new ReentrantLock();

    public StockService(ProductMasterRepository productMasterRepository,
                        InventoryRepository inventoryRepository,
                        SaleRepository saleRepository) {
        this.productMasterRepository = productMasterRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
    }

    // ── Get all products with current stock ─────────────────────────────────────
    public List<ProductMasterResponse> getAllProducts(String status, String search, Integer categoryId) {
        List<ProductMasterEntity> products;

        if (search != null && !search.isBlank()) {
            RecordStatusEnum statusEnum = status != null ? parseStatus(status) : RecordStatusEnum.ACTIVE;
            products = productMasterRepository.searchProducts(search.trim(), statusEnum);
        } else if (categoryId != null) {
            RecordStatusEnum statusEnum = status != null ? parseStatus(status) : RecordStatusEnum.ACTIVE;
            products = productMasterRepository.findByCategoryIdAndStatus(categoryId, statusEnum);
        } else if (status != null) {
            products = productMasterRepository.findByStatus(parseStatus(status));
        } else {
            products = productMasterRepository.findByStatus(RecordStatusEnum.ACTIVE);
        }

        return products.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // ── Get single product ───────────────────────────────────────────────────────
    public ProductMasterResponse getProductById(Integer id) {
        ProductMasterEntity product = productMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToResponse(product);
    }

    // ── Get full transaction history for a product (purchases + sales) ───────────
    public List<StockTransactionResponse> getTransactionHistory(Integer productMasterId) {
        // Verify product exists
        productMasterRepository.findById(productMasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productMasterId));

        List<StockTransactionResponse> transactions = new ArrayList<>();

        // Purchases: all inventory records linked to this product master
        List<InventoryEntity> purchases = inventoryRepository.findByProductMasterIdAndStatus(
                productMasterId, RecordStatusEnum.ACTIVE);
        for (InventoryEntity inv : purchases) {
            StockTransactionResponse tx = new StockTransactionResponse();
            tx.setTransactionId(inv.getId());
            tx.setType(StockTransactionResponse.TransactionType.PURCHASE);
            tx.setQuantity(inv.getQuantity() != null ? inv.getQuantity() : BigDecimal.ZERO);
            tx.setRate(inv.getPurchaseRate());
            if (inv.getQuantity() != null && inv.getPurchaseRate() != null) {
                tx.setTotalValue(inv.getQuantity().multiply(inv.getPurchaseRate()).setScale(2, RoundingMode.HALF_UP));
            }
            tx.setTransactionDate(inv.getCreatedAt() != null ? inv.getCreatedAt().toLocalDate() : null);
            tx.setParty(inv.getManufacturerName());
            tx.setReference(inv.getName());
            tx.setRecordedBy("system");
            tx.setCreatedAt(inv.getCreatedAt());
            transactions.add(tx);
        }

        // Sales: all sale records linked to this product master
        List<SaleEntity> sales = saleRepository.findByProductMasterIdAndStatusOrderByCreatedAtDesc(
                productMasterId, RecordStatusEnum.ACTIVE);
        for (SaleEntity sale : sales) {
            StockTransactionResponse tx = new StockTransactionResponse();
            tx.setTransactionId(sale.getId());
            tx.setType(StockTransactionResponse.TransactionType.SALE);
            tx.setQuantity(sale.getQuantity());
            tx.setRate(sale.getSellingRate());
            if (sale.getQuantity() != null && sale.getSellingRate() != null) {
                tx.setTotalValue(sale.getQuantity().multiply(sale.getSellingRate()).setScale(2, RoundingMode.HALF_UP));
            }
            tx.setTransactionDate(sale.getSaleDate());
            tx.setParty(sale.getCustomerName());
            tx.setReference(sale.getNotes());
            tx.setRecordedBy(sale.getRecordedBy());
            tx.setCreatedAt(sale.getCreatedAt());
            transactions.add(tx);
        }

        // Sort by date descending
        transactions.sort(Comparator.comparing(StockTransactionResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return transactions;
    }

    // ── Internal: find or create ProductMaster for a category+subCategory ────────
    // Called by InvoiceExtractionService when saving bill items.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public ProductMasterEntity findOrCreateProductMaster(CategoryEntity category,
                                                          SubCategoryEntity subCategory,
                                                          String hsnCode,
                                                          UnitOfMeasurementEntity unit) {
        return productMasterRepository
                .findByCategoryIdAndSubCategoryId(category.getId(), subCategory.getId())
                .orElseGet(() -> {
                    try {
                        ProductMasterEntity pm = new ProductMasterEntity();
                        pm.setCategory(category);
                        pm.setSubCategory(subCategory);
                        pm.setHsnCode(hsnCode);
                        pm.setUnitOfMeasurement(unit);
                        pm.setCurrentStock(BigDecimal.ZERO);
                        pm.setStatus(RecordStatusEnum.ACTIVE);
                        ProductMasterEntity saved = productMasterRepository.saveAndFlush(pm);
                        log.info("Created new ProductMaster for category='{}' subCategory='{}'",
                                category.getName(), subCategory.getName());
                        return saved;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.info("Race condition detected for category='{}' subCategory='{}', re-fetching existing...",
                                category.getName(), subCategory.getName());
                        return productMasterRepository
                                .findByCategoryIdAndSubCategoryId(category.getId(), subCategory.getId())
                                .orElseThrow(() -> new RuntimeException("Failed to recover from race condition during ProductMaster creation", e));
                    }
                });
    }

    // ── Internal: add incoming stock (called on invoice save) ────────────────────
    // Applies weighted average for purchaseRate.
    @Transactional
    public void addStock(ProductMasterEntity product, BigDecimal incomingQty, BigDecimal purchaseRate) {
        if (incomingQty == null || incomingQty.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal oldStock = product.getCurrentStock() != null ? product.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal oldRate  = product.getAveragePurchaseRate() != null ? product.getAveragePurchaseRate() : BigDecimal.ZERO;

        BigDecimal newStock = oldStock.add(incomingQty);

        // Weighted average: (oldStock * oldRate + incomingQty * newRate) / newStock
        if (purchaseRate != null && purchaseRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weightedSum = oldStock.multiply(oldRate).add(incomingQty.multiply(purchaseRate));
            BigDecimal newAvgRate  = weightedSum.divide(newStock, 2, RoundingMode.HALF_UP);
            product.setAveragePurchaseRate(newAvgRate);
        }

        product.setCurrentStock(newStock);
        productMasterRepository.save(product);
        log.info("Stock added — product ID {}: +{} → total stock {}",
                product.getId(), incomingQty, newStock);
    }

    // ── Internal: deduct stock (called on sale) ──────────────────────────────────
    @Transactional
    public void deductStock(ProductMasterEntity product, BigDecimal qty) {
        BigDecimal current = product.getCurrentStock() != null ? product.getCurrentStock() : BigDecimal.ZERO;
        if (current.compareTo(qty) < 0) {
            throw new IllegalStateException(
                    String.format("Insufficient stock. Available: %s, Requested: %s", current, qty));
        }
        product.setCurrentStock(current.subtract(qty));
        productMasterRepository.save(product);
        log.info("Stock deducted — product ID {}: -{} → remaining {}",
                product.getId(), qty, product.getCurrentStock());
    }

    // ── Recalculate weighted average purchase rate from all remaining ACTIVE inventory
    // Call this after an InventoryEntity is soft-deleted so that the rate on
    // ProductMaster reflects only what is still physically in stock.
    @Transactional
    public void recalculateAveragePurchaseRate(ProductMasterEntity product) {
        List<InventoryEntity> activeRecords = inventoryRepository.findByProductMasterIdAndStatus(
                product.getId(), RecordStatusEnum.ACTIVE);

        if (activeRecords.isEmpty()) {
            // All purchase records deleted — reset rate to zero
            product.setAveragePurchaseRate(BigDecimal.ZERO);
        } else {
            BigDecimal totalValue = BigDecimal.ZERO;
            BigDecimal totalQty   = BigDecimal.ZERO;
            for (InventoryEntity inv : activeRecords) {
                BigDecimal qty  = inv.getQuantity()     != null ? inv.getQuantity()     : BigDecimal.ZERO;
                BigDecimal rate = inv.getPurchaseRate() != null ? inv.getPurchaseRate() : BigDecimal.ZERO;
                totalValue = totalValue.add(qty.multiply(rate));
                totalQty   = totalQty.add(qty);
            }
            if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                product.setAveragePurchaseRate(
                        totalValue.divide(totalQty, 2, RoundingMode.HALF_UP));
            }
        }
        productMasterRepository.save(product);
        log.info("Recalculated averagePurchaseRate for product ID {}: {}",
                product.getId(), product.getAveragePurchaseRate());
    }

    // ── Update selling rate for a product ────────────────────────────────────────
    @Transactional
    public ProductMasterResponse updateSellingRate(Integer id, BigDecimal sellingRate) {
        ProductMasterEntity product = productMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        if (sellingRate == null || sellingRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Selling rate must be a non-negative value.");
        }
        product.setSellingRate(sellingRate);
        productMasterRepository.save(product);
        log.info("Selling rate updated for product ID {}: {}", id, sellingRate);
        return convertToResponse(product);
    }

    // ── Convert entity → response DTO ───────────────────────────────────────────
    public ProductMasterResponse convertToResponse(ProductMasterEntity pm) {
        ProductMasterResponse r = new ProductMasterResponse();
        r.setId(pm.getId());

        if (pm.getCategory() != null) {
            r.setCategoryId(pm.getCategory().getId());
            r.setCategoryName(pm.getCategory().getName());
        }
        if (pm.getSubCategory() != null) {
            r.setSubCategoryId(pm.getSubCategory().getId());
            r.setSubCategoryName(pm.getSubCategory().getName());
        }
        r.setHsnCode(pm.getHsnCode());
        if (pm.getUnitOfMeasurement() != null) {
            r.setUnitOfMeasurementId(pm.getUnitOfMeasurement().getId());
            r.setUnitOfMeasurementName(pm.getUnitOfMeasurement().getName());
            r.setUnitOfMeasurementAbbreviation(pm.getUnitOfMeasurement().getAbbreviation());
        }
        r.setCurrentStock(pm.getCurrentStock());
        r.setAveragePurchaseRate(pm.getAveragePurchaseRate());
        r.setSellingRate(pm.getSellingRate());

        // totalStockValue = currentStock * averagePurchaseRate
        if (pm.getCurrentStock() != null && pm.getAveragePurchaseRate() != null) {
            r.setTotalStockValue(
                    pm.getCurrentStock().multiply(pm.getAveragePurchaseRate()).setScale(2, RoundingMode.HALF_UP));
        }
        r.setStatus(pm.getStatus());
        r.setCreatedAt(pm.getCreatedAt());
        r.setUpdatedAt(pm.getUpdatedAt());
        return r;
    }

    // ── Backfill: link existing inventory records to ProductMaster ────────────────
    // Call once to consolidate historical bill data into the stock ledger.
    // Safe to call multiple times — only processes items not yet linked.
    @Transactional
    public Map<String, Object> backfillFromInventory() {
        if (!backfillLock.tryLock()) {
            throw new RuntimeException("Backfill is currently in progress. Please wait.");
        }
        try {
            List<InventoryEntity> allActive = inventoryRepository.findByStatus(RecordStatusEnum.ACTIVE);

        int skipped = 0;
        List<InventoryEntity> linkable = new ArrayList<>();
        for (InventoryEntity inv : allActive) {
            if (inv.getCategory() == null || inv.getSubCategory() == null) {
                skipped++;
            } else if (inv.getProductMaster() == null) {
                linkable.add(inv);
            }
        }

        if (linkable.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("productsCreated", 0);
            result.put("itemsLinked", 0);
            result.put("itemsSkipped", skipped);
            result.put("message", "All eligible items are already linked to consolidated stock.");
            return result;
        }

        // Group unlinked items by (categoryId_subCategoryId)
        Map<String, List<InventoryEntity>> groups = linkable.stream()
                .collect(Collectors.groupingBy(i ->
                        i.getCategory().getId() + "_" + i.getSubCategory().getId()));

        int productsCreated = 0;
        int itemsLinked = 0;

        for (List<InventoryEntity> group : groups.values()) {
            InventoryEntity first = group.get(0);

            boolean alreadyExists = productMasterRepository
                    .findByCategoryIdAndSubCategoryId(first.getCategory().getId(), first.getSubCategory().getId())
                    .isPresent();

            ProductMasterEntity pm = findOrCreateProductMaster(
                    first.getCategory(),
                    first.getSubCategory(),
                    first.getHsnCode(),
                    first.getUnitOfMeasurement());

            if (!alreadyExists) productsCreated++;

            for (InventoryEntity inv : group) {
                BigDecimal qty = inv.getQuantity() != null ? inv.getQuantity() : BigDecimal.ZERO;
                addStock(pm, qty, inv.getPurchaseRate());
                inv.setProductMaster(pm);
                inventoryRepository.save(inv);
                itemsLinked++;
            }
        }

        log.info("Backfill complete: {} new product slots, {} items linked, {} skipped",
                productsCreated, itemsLinked, skipped);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productsCreated", productsCreated);
        result.put("itemsLinked", itemsLinked);
        result.put("itemsSkipped", skipped);
        result.put("message", String.format(
                "Backfill complete. Created %d product slot(s), linked %d purchase record(s).",
                productsCreated, itemsLinked));
        return result;
        } finally {
            backfillLock.unlock();
        }
    }

    // ── MEDIUM-4: Supplier-level stock breakdown ─────────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSupplierStockBreakdown() {
        List<Object[]> rows = inventoryRepository.getSupplierStockSummary(RecordStatusEnum.ACTIVE);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("supplierId", row[0]);
            entry.put("supplierName", row[1]);
            entry.put("lineItemCount", row[2]);
            entry.put("totalQuantity", row[3]);
            entry.put("totalValue", ((java.math.BigDecimal) row[4]).setScale(2, RoundingMode.HALF_UP));
            result.add(entry);
        }
        return result;
    }

    private RecordStatusEnum parseStatus(String status) {
        try {
            return RecordStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status + ". Must be ACTIVE or DELETED.");
        }
    }
}

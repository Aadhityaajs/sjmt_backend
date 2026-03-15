package com.sjmt.SJMT.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sjmt.SJMT.DTO.RequestDTO.CreateQuotationRequest;
import com.sjmt.SJMT.DTO.RequestDTO.QuotationItemRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.QuotationItemResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.QuotationResponse;
import com.sjmt.SJMT.Entity.*;
import com.sjmt.SJMT.Repository.CustomerRepository;
import com.sjmt.SJMT.Repository.ProductMasterRepository;
import com.sjmt.SJMT.Repository.QuotationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    private static final Logger log = LoggerFactory.getLogger(QuotationService.class);

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final ProductMasterRepository productMasterRepository;

    public QuotationService(QuotationRepository quotationRepository,
                            CustomerRepository customerRepository,
                            ProductMasterRepository productMasterRepository) {
        this.quotationRepository = quotationRepository;
        this.customerRepository = customerRepository;
        this.productMasterRepository = productMasterRepository;
    }

    // ── Create Quotation (DRAFT) — NO stock deduction ────────────────────────────
    @Transactional
    public QuotationResponse createQuotation(CreateQuotationRequest request, String username) {
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

        if (customer.getStatus() == CustomerStatusEnum.BLACKLISTED) {
            throw new IllegalStateException("Cannot create a quotation for BLACKLISTED customer: " + customer.getCustomerName());
        }

        QuotationEntity quotation = new QuotationEntity();
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setCustomer(customer);
        quotation.setCustomerSnapshot(buildCustomerSnapshot(customer));
        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setInterstate(request.isInterstate());
        quotation.setNotes(request.getNotes());
        quotation.setTerms(request.getTerms());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setCreatedBy(username);

        List<QuotationItemEntity> items = buildQuotationItems(request.getItems(), quotation, request.isInterstate());
        quotation.setItems(items);
        calculateQuotationTotals(quotation);

        QuotationEntity saved = quotationRepository.save(quotation);
        log.info("Quotation {} created by {}", saved.getQuotationNumber(), username);
        return toResponse(saved);
    }

    // ── Update Quotation (DRAFT only) ─────────────────────────────────────────────
    @Transactional
    public QuotationResponse updateQuotation(Integer id, CreateQuotationRequest request, String username) {
        QuotationEntity quotation = findById(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT quotations can be edited. Current status: " + quotation.getStatus());
        }

        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

        quotation.setCustomer(customer);
        quotation.setCustomerSnapshot(buildCustomerSnapshot(customer));
        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setInterstate(request.isInterstate());
        quotation.setNotes(request.getNotes());
        quotation.setTerms(request.getTerms());

        // Replace all items (orphanRemoval = true handles deletes)
        quotation.getItems().clear();
        List<QuotationItemEntity> newItems = buildQuotationItems(request.getItems(), quotation, request.isInterstate());
        quotation.getItems().addAll(newItems);
        calculateQuotationTotals(quotation);

        QuotationEntity saved = quotationRepository.save(quotation);
        log.info("Quotation {} updated by {}", saved.getQuotationNumber(), username);
        return toResponse(saved);
    }

    // ── Change Status (SENT / ACCEPTED / REJECTED) ────────────────────────────────
    @Transactional
    public QuotationResponse changeStatus(Integer id, QuotationStatus newStatus, String username) {
        QuotationEntity quotation = findById(id);
        validateStatusTransition(quotation.getStatus(), newStatus);
        quotation.setStatus(newStatus);
        QuotationEntity saved = quotationRepository.save(quotation);
        log.info("Quotation {} status changed to {} by {}", saved.getQuotationNumber(), newStatus, username);
        return toResponse(saved);
    }

    // ── Get One ───────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public QuotationResponse getById(Integer id) {
        return toResponse(findById(id));
    }

    // ── Get All (paginated, optional filters) ─────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<QuotationResponse> getAll(int page, int size, QuotationStatus status, Integer customerId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && customerId != null) {
            return quotationRepository.findByStatusAndCustomer_CustomerId(status, customerId, pageable)
                    .map(this::toResponse);
        } else if (status != null) {
            return quotationRepository.findByStatus(status, pageable).map(this::toResponse);
        } else if (customerId != null) {
            return quotationRepository.findByCustomer_CustomerId(customerId, pageable).map(this::toResponse);
        }
        return quotationRepository.findAll(pageable).map(this::toResponse);
    }

    // ── Delete (DRAFT only) ───────────────────────────────────────────────────────
    @Transactional
    public void deleteQuotation(Integer id, String username) {
        QuotationEntity quotation = findById(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT quotations can be deleted. Current status: " + quotation.getStatus());
        }
        quotationRepository.delete(quotation);
        log.info("Quotation {} deleted by {}", quotation.getQuotationNumber(), username);
    }

    // ── Convert ACCEPTED Quotation → CustomerBillEntity skeleton ─────────────────
    // Returns the quotation; CustomerBillService will call this to get the source data
    @Transactional
    public QuotationEntity getAcceptedQuotationForConversion(Integer quotationId) {
        QuotationEntity quotation = findById(quotationId);
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED quotations can be converted to a bill. Current status: " + quotation.getStatus());
        }
        return quotation;
    }

    // Mark quotation as CONVERTED (called by CustomerBillService after bill creation)
    @Transactional
    public void markAsConverted(Integer quotationId) {
        QuotationEntity quotation = findById(quotationId);
        quotation.setStatus(QuotationStatus.CONVERTED);
        quotationRepository.save(quotation);
    }

    // ── Auto-expire quotations past their validUntil date ─────────────────────────
    @Transactional
    public void expireOutdatedQuotations() {
        List<QuotationEntity> all = quotationRepository.findAll();
        LocalDate today = LocalDate.now();
        all.stream()
                .filter(q -> (q.getStatus() == QuotationStatus.DRAFT || q.getStatus() == QuotationStatus.SENT)
                        && q.getValidUntil() != null
                        && q.getValidUntil().isBefore(today))
                .forEach(q -> {
                    q.setStatus(QuotationStatus.EXPIRED);
                    quotationRepository.save(q);
                    log.info("Quotation {} auto-expired", q.getQuotationNumber());
                });
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private QuotationEntity findById(Integer id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + id));
    }

    private String generateQuotationNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = quotationRepository.countByQuotationNumberStartingWith(year);
        return year + (count + 1);
    }

    private List<QuotationItemEntity> buildQuotationItems(List<QuotationItemRequest> requests,
                                                          QuotationEntity quotation,
                                                          boolean interstate) {
        List<QuotationItemEntity> items = new ArrayList<>();
        for (QuotationItemRequest req : requests) {
            QuotationItemEntity item = new QuotationItemEntity();
            item.setQuotation(quotation);

            if (req.getProductMasterId() != null) {
                ProductMasterEntity product = productMasterRepository.findById(req.getProductMasterId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductMasterId()));
                item.setProduct(product);
            }

            item.setDescription(req.getDescription());
            item.setHsnCode(req.getHsnCode());
            item.setQuantity(req.getQuantity());
            item.setUnit(req.getUnit());
            item.setUnitPrice(req.getUnitPrice());

            BigDecimal discountPct = req.getDiscountPct() != null ? req.getDiscountPct() : BigDecimal.ZERO;
            BigDecimal gstPct = req.getGstPct() != null ? req.getGstPct() : BigDecimal.ZERO;
            item.setDiscountPct(discountPct);
            item.setGstPct(gstPct);

            computeItemTax(item, interstate);
            items.add(item);
        }
        return items;
    }

    /**
     * Calculates per-line tax fields and lineTotal.
     *
     * taxableAmount = (qty × unitPrice) − discountAmount
     * Intrastate: cgst = taxable × gstPct/200, sgst = taxable × gstPct/200
     * Interstate:  igst = taxable × gstPct/100
     * lineTotal = taxableAmount + cgst + sgst  OR  taxableAmount + igst
     */
    private void computeItemTax(QuotationItemEntity item, boolean interstate) {
        BigDecimal gross = item.getQuantity().multiply(item.getUnitPrice());
        BigDecimal discountAmount = gross.multiply(item.getDiscountPct())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxable = gross.subtract(discountAmount);

        item.setDiscountAmount(discountAmount);

        if (interstate) {
            BigDecimal igst = taxable.multiply(item.getGstPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setIgstAmount(igst);
            item.setCgstAmount(BigDecimal.ZERO);
            item.setSgstAmount(BigDecimal.ZERO);
            item.setLineTotal(taxable.add(igst));
        } else {
            BigDecimal halfGst = taxable.multiply(item.getGstPct())
                    .divide(BigDecimal.valueOf(200), 2, RoundingMode.HALF_UP);
            item.setCgstAmount(halfGst);
            item.setSgstAmount(halfGst);
            item.setIgstAmount(BigDecimal.ZERO);
            item.setLineTotal(taxable.add(halfGst).add(halfGst));
        }
    }

    private void calculateQuotationTotals(QuotationEntity quotation) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (QuotationItemEntity item : quotation.getItems()) {
            subtotal = subtotal.add(item.getQuantity().multiply(item.getUnitPrice()));
            totalDiscount = totalDiscount.add(item.getDiscountAmount());
            totalCgst = totalCgst.add(item.getCgstAmount());
            totalSgst = totalSgst.add(item.getSgstAmount());
            totalIgst = totalIgst.add(item.getIgstAmount());
            totalAmount = totalAmount.add(item.getLineTotal());
        }

        quotation.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        quotation.setTotalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP));
        quotation.setTotalCgst(totalCgst.setScale(2, RoundingMode.HALF_UP));
        quotation.setTotalSgst(totalSgst.setScale(2, RoundingMode.HALF_UP));
        quotation.setTotalIgst(totalIgst.setScale(2, RoundingMode.HALF_UP));
        quotation.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
    }

    private void validateStatusTransition(QuotationStatus current, QuotationStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == QuotationStatus.SENT;
            case SENT -> next == QuotationStatus.ACCEPTED || next == QuotationStatus.REJECTED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    private String buildCustomerSnapshot(CustomerEntity c) {
        try {
            Map<String, Object> snap = new HashMap<>();
            snap.put("customerName", c.getCustomerName());
            snap.put("address", c.getAddress());
            snap.put("city", c.getCity());
            snap.put("state", c.getState());
            snap.put("pincode", c.getPincode());
            snap.put("phoneNumber", c.getPhoneNumber());
            snap.put("gstNumber", c.getGstNumber());
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(snap);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ── Entity → Response ─────────────────────────────────────────────────────────
    public QuotationResponse toResponse(QuotationEntity q) {
        QuotationResponse r = new QuotationResponse();
        r.setId(q.getId());
        r.setQuotationNumber(q.getQuotationNumber());
        r.setCustomerId(q.getCustomer().getCustomerId());
        r.setCustomerName(q.getCustomer().getCustomerName());
        r.setCustomerSnapshot(q.getCustomerSnapshot());
        r.setQuotationDate(q.getQuotationDate());
        r.setValidUntil(q.getValidUntil());
        r.setStatus(q.getStatus());
        r.setInterstate(q.isInterstate());
        r.setSubtotal(q.getSubtotal());
        r.setTotalDiscount(q.getTotalDiscount());
        r.setTotalCgst(q.getTotalCgst());
        r.setTotalSgst(q.getTotalSgst());
        r.setTotalIgst(q.getTotalIgst());
        r.setTotalAmount(q.getTotalAmount());
        r.setNotes(q.getNotes());
        r.setTerms(q.getTerms());
        r.setCreatedBy(q.getCreatedBy());
        r.setCreatedAt(q.getCreatedAt());
        r.setUpdatedAt(q.getUpdatedAt());

        List<QuotationItemResponse> itemResponses = q.getItems().stream().map(item -> {
            QuotationItemResponse ir = new QuotationItemResponse();
            ir.setId(item.getId());
            ir.setProductMasterId(item.getProduct() != null ? item.getProduct().getId() : null);
            ir.setDescription(item.getDescription());
            ir.setHsnCode(item.getHsnCode());
            ir.setQuantity(item.getQuantity());
            ir.setUnit(item.getUnit());
            ir.setUnitPrice(item.getUnitPrice());
            ir.setDiscountPct(item.getDiscountPct());
            ir.setDiscountAmount(item.getDiscountAmount());
            ir.setGstPct(item.getGstPct());
            ir.setCgstAmount(item.getCgstAmount());
            ir.setSgstAmount(item.getSgstAmount());
            ir.setIgstAmount(item.getIgstAmount());
            ir.setLineTotal(item.getLineTotal());
            return ir;
        }).collect(Collectors.toList());

        r.setItems(itemResponses);
        return r;
    }
}
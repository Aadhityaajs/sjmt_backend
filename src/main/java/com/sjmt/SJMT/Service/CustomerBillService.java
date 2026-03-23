package com.sjmt.SJMT.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sjmt.SJMT.DTO.RequestDTO.CreateCustomerBillRequest;
import com.sjmt.SJMT.DTO.RequestDTO.CustomerBillItemRequest;
import com.sjmt.SJMT.DTO.RequestDTO.RecordPaymentRequest;
import com.sjmt.SJMT.DTO.RequestDTO.RecordSaleRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.BillPaymentResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.CustomerBillItemResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.CustomerBillResponse;
import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.sjmt.SJMT.Entity.*;
import com.sjmt.SJMT.Entity.CustomerStatusEnum;
import com.sjmt.SJMT.Repository.BillPaymentRepository;
import com.sjmt.SJMT.Repository.CustomerBillRepository;
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
public class CustomerBillService {

    private static final Logger log = LoggerFactory.getLogger(CustomerBillService.class);

    private final CustomerBillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final ProductMasterRepository productMasterRepository;
    private final QuotationRepository quotationRepository;
    private final BillPaymentRepository paymentRepository;
    private final SaleService saleService;
    private final QuotationService quotationService;
    private final AuditService auditService;

    public CustomerBillService(CustomerBillRepository billRepository,
                               CustomerRepository customerRepository,
                               ProductMasterRepository productMasterRepository,
                               QuotationRepository quotationRepository,
                               BillPaymentRepository paymentRepository,
                               SaleService saleService,
                               QuotationService quotationService,
                               AuditService auditService) {
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.productMasterRepository = productMasterRepository;
        this.quotationRepository = quotationRepository;
        this.paymentRepository = paymentRepository;
        this.saleService = saleService;
        this.quotationService = quotationService;
        this.auditService = auditService;
    }

    // ── Create Bill (DRAFT) — NO stock deduction yet ──────────────────────────────
    @Transactional
    public CustomerBillResponse createBill(CreateCustomerBillRequest request, String username) {
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        if (customer.getStatus() == CustomerStatusEnum.BLACKLISTED) {
            throw new IllegalStateException("Cannot create a bill for BLACKLISTED customer: " + customer.getCustomerName());
        }

        CustomerBillEntity bill = new CustomerBillEntity();
        bill.setBillNumber(generateBillNumber());
        bill.setCustomer(customer);
        bill.setCustomerSnapshot(buildCustomerSnapshot(customer));
        bill.setBillDate(request.getBillDate());
        bill.setDueDate(request.getDueDate());
        bill.setInterstate(request.isInterstate());
        bill.setNotes(request.getNotes());
        bill.setTerms(request.getTerms());
        bill.setStatus(BillStatus.DRAFT);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStockDeducted(false);
        bill.setCreatedBy(username);

        List<CustomerBillItemEntity> items = buildBillItems(request.getItems(), bill, request.isInterstate());
        bill.setItems(items);
        calculateBillTotals(bill);

        CustomerBillEntity saved = billRepository.save(bill);
        log.info("Bill {} created (DRAFT) by {}", saved.getBillNumber(), username);
        auditService.record("CustomerBill", String.valueOf(saved.getId()), "BILL_CREATED",
                username, "Bill# " + saved.getBillNumber() + ", Customer: " + customer.getCustomerName()
                        + ", Total: " + saved.getTotalAmount());
        return toResponse(saved);
    }

    // ── Convert Accepted Quotation → Bill ─────────────────────────────────────────
    @Transactional
    public CustomerBillResponse convertFromQuotation(Integer quotationId, LocalDate billDate,
                                                     LocalDate dueDate, String username) {
        QuotationEntity quotation = quotationService.getAcceptedQuotationForConversion(quotationId);

        CustomerBillEntity bill = new CustomerBillEntity();
        bill.setBillNumber(generateBillNumber());
        bill.setQuotation(quotation);
        bill.setCustomer(quotation.getCustomer());
        bill.setCustomerSnapshot(quotation.getCustomerSnapshot());
        bill.setBillDate(billDate != null ? billDate : LocalDate.now());
        bill.setDueDate(dueDate != null ? dueDate : LocalDate.now().plusDays(30));
        bill.setInterstate(quotation.isInterstate());
        bill.setNotes(quotation.getNotes());
        bill.setTerms(quotation.getTerms());
        bill.setStatus(BillStatus.DRAFT);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStockDeducted(false);
        bill.setCreatedBy(username);

        // Copy quotation items → bill items
        List<CustomerBillItemEntity> items = new ArrayList<>();
        for (QuotationItemEntity qi : quotation.getItems()) {
            CustomerBillItemEntity bi = new CustomerBillItemEntity();
            bi.setBill(bill);
            bi.setProduct(qi.getProduct());
            bi.setDescription(qi.getDescription());
            bi.setHsnCode(qi.getHsnCode());
            bi.setQuantity(qi.getQuantity());
            bi.setUnit(qi.getUnit());
            bi.setUnitPrice(qi.getUnitPrice());
            bi.setDiscountPct(qi.getDiscountPct());
            bi.setDiscountAmount(qi.getDiscountAmount());
            bi.setGstPct(qi.getGstPct());
            bi.setCgstAmount(qi.getCgstAmount());
            bi.setSgstAmount(qi.getSgstAmount());
            bi.setIgstAmount(qi.getIgstAmount());
            bi.setLineTotal(qi.getLineTotal());
            items.add(bi);
        }
        bill.setItems(items);
        calculateBillTotals(bill);

        CustomerBillEntity saved = billRepository.save(bill);

        // Mark the source quotation as CONVERTED
        quotationService.markAsConverted(quotationId);

        log.info("Bill {} created from Quotation {} by {}", saved.getBillNumber(),
                quotation.getQuotationNumber(), username);
        return toResponse(saved);
    }

    // ── Update Bill (DRAFT only) ───────────────────────────────────────────────────
    @Transactional
    public CustomerBillResponse updateBill(Integer id, CreateCustomerBillRequest request, String username) {
        CustomerBillEntity bill = findById(id);

        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT bills can be edited. Current status: " + bill.getStatus());
        }

        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        bill.setCustomer(customer);
        bill.setCustomerSnapshot(buildCustomerSnapshot(customer));
        bill.setBillDate(request.getBillDate());
        bill.setDueDate(request.getDueDate());
        bill.setInterstate(request.isInterstate());
        bill.setNotes(request.getNotes());
        bill.setTerms(request.getTerms());

        // Replace all items
        bill.getItems().clear();
        List<CustomerBillItemEntity> newItems = buildBillItems(request.getItems(), bill, request.isInterstate());
        bill.getItems().addAll(newItems);
        calculateBillTotals(bill);

        bill.setUpdatedBy(username);
        CustomerBillEntity saved = billRepository.save(bill);
        log.info("Bill {} updated by {}", saved.getBillNumber(), username);
        return toResponse(saved);
    }

    // ── Issue Bill → triggers stock deduction ─────────────────────────────────────
    @Transactional
    public CustomerBillResponse issueBill(Integer id, String username) {
        CustomerBillEntity bill = findById(id);

        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT bills can be issued. Current status: " + bill.getStatus());
        }

        // Guard: only deduct stock once (prevents double deduction if called twice)
        if (!bill.isStockDeducted()) {
            // If this bill was created from an ACCEPTED quotation that already reserved
            // stock, skip the deduction — stock was taken at quotation acceptance time.
            boolean alreadyReserved = bill.getQuotation() != null
                    && bill.getQuotation().isStockReserved();

            for (CustomerBillItemEntity item : bill.getItems()) {
                if (item.getProduct() != null) {
                    RecordSaleRequest saleReq = new RecordSaleRequest();
                    saleReq.setProductMasterId(item.getProduct().getId());
                    saleReq.setQuantity(item.getQuantity());
                    saleReq.setSellingRate(item.getUnitPrice()); // price locked at bill creation
                    saleReq.setSaleDate(bill.getBillDate());
                    saleReq.setCustomerId(bill.getCustomer().getCustomerId()); // FK link (HIGH-6)
                    saleReq.setCustomerName(bill.getCustomer().getCustomerName());
                    saleReq.setNotes("Bill #" + bill.getBillNumber());
                    if (alreadyReserved) {
                        // Stock already deducted at reservation — only create the sale record
                        saleService.recordSaleWithoutStockDeduction(saleReq, username);
                    } else {
                        // Standard path: validate stock, deduct, and save SaleEntity
                        saleService.recordSale(saleReq, username);
                    }
                }
            }
            // Mark quotation reservation as consumed
            if (alreadyReserved) {
                bill.getQuotation().setStockReserved(false);
                quotationRepository.save(bill.getQuotation());
            }
            bill.setStockDeducted(true);
        }

        bill.setStatus(BillStatus.ISSUED);
        bill.setUpdatedBy(username);
        CustomerBillEntity saved = billRepository.save(bill);
        log.info("Bill {} ISSUED by {} — stock deducted for {} item(s)",
                saved.getBillNumber(), username, bill.getItems().size());
        auditService.record("CustomerBill", String.valueOf(saved.getId()), "BILL_ISSUED",
                username, "Bill# " + saved.getBillNumber() + ", Total: " + saved.getTotalAmount()
                        + ", Items: " + bill.getItems().size());
        return toResponse(saved);
    }

    // ── Record Payment ─────────────────────────────────────────────────────────────
    @Transactional
    public CustomerBillResponse recordPayment(Integer id, RecordPaymentRequest request, String username) {
        CustomerBillEntity bill = findById(id);

        if (bill.getStatus() == BillStatus.DRAFT || bill.getStatus() == BillStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot record payment for a bill with status: " + bill.getStatus());
        }

        BigDecimal newPaid = bill.getPaidAmount().add(request.getAmount());
        if (newPaid.compareTo(bill.getTotalAmount()) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment amount ₹%s exceeds remaining balance ₹%s",
                            request.getAmount(), bill.getBalanceAmount()));
        }

        bill.setPaidAmount(newPaid);
        bill.setBalanceAmount(bill.getTotalAmount().subtract(newPaid));
        bill.setPaymentMode(request.getPaymentMode());
        bill.setPaymentReference(request.getPaymentReference());

        // Auto-update status based on payment
        if (newPaid.compareTo(bill.getTotalAmount()) >= 0) {
            bill.setStatus(BillStatus.PAID);
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        CustomerBillEntity saved = billRepository.save(bill);

        // Append to payment ledger
        BillPaymentEntity ledgerEntry = new BillPaymentEntity();
        ledgerEntry.setBill(saved);
        ledgerEntry.setAmount(request.getAmount());
        ledgerEntry.setPaymentMode(request.getPaymentMode());
        ledgerEntry.setPaymentReference(request.getPaymentReference());
        ledgerEntry.setPaymentDate(request.getPaymentDate());
        ledgerEntry.setEntryType("PAYMENT");
        ledgerEntry.setRecordedBy(username);
        paymentRepository.save(ledgerEntry);

        log.info("Payment ₹{} recorded on Bill {} by {} — new balance: ₹{}",
                request.getAmount(), saved.getBillNumber(), username, saved.getBalanceAmount());
        auditService.record("CustomerBill", String.valueOf(saved.getId()), "PAYMENT_RECORDED",
                username, "Bill# " + saved.getBillNumber() + ", Amount: ₹" + request.getAmount()
                        + ", Mode: " + request.getPaymentMode()
                        + ", Ref: " + request.getPaymentReference()
                        + ", Balance: ₹" + saved.getBalanceAmount());
        return toResponse(saved);
    }

    // ── Reverse a payment (e.g. bounced cheque) ───────────────────────────────────
    @Transactional
    public CustomerBillResponse reversePayment(Integer billId, Integer paymentId,
                                               String reason, String username) {
        CustomerBillEntity bill = findById(billId);
        BillPaymentEntity original = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (!original.getBill().getId().equals(billId)) {
            throw new IllegalArgumentException("Payment ID " + paymentId + " does not belong to Bill ID " + billId);
        }
        if ("REVERSAL".equals(original.getEntryType())) {
            throw new IllegalStateException("Cannot reverse an entry that is already a reversal.");
        }

        // Reverse the paid amount on the bill
        BigDecimal reversedPaid = bill.getPaidAmount().subtract(original.getAmount());
        if (reversedPaid.compareTo(BigDecimal.ZERO) < 0) {
            reversedPaid = BigDecimal.ZERO;
        }
        bill.setPaidAmount(reversedPaid);
        bill.setBalanceAmount(bill.getTotalAmount().subtract(reversedPaid));

        // Update bill status accordingly
        if (reversedPaid.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.ISSUED);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        CustomerBillEntity saved = billRepository.save(bill);

        // Write reversal entry into the ledger (negative amount)
        BillPaymentEntity reversal = new BillPaymentEntity();
        reversal.setBill(saved);
        reversal.setAmount(original.getAmount().negate());
        reversal.setPaymentMode(original.getPaymentMode());
        reversal.setPaymentReference(original.getPaymentReference());
        reversal.setPaymentDate(java.time.LocalDate.now());
        reversal.setEntryType("REVERSAL");
        reversal.setReversalReason(reason);
        reversal.setRecordedBy(username);
        paymentRepository.save(reversal);

        log.info("Payment ID {} reversed on Bill {} by {} — reason: {}",
                paymentId, saved.getBillNumber(), username, reason);
        auditService.record("CustomerBill", String.valueOf(saved.getId()), "PAYMENT_REVERSED",
                username, "Bill# " + saved.getBillNumber() + ", Reversed PaymentID: " + paymentId
                        + ", Amount: ₹" + original.getAmount() + ", Reason: " + reason);
        return toResponse(saved);
    }

    // ── Get payment ledger for a bill ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<BillPaymentResponse> getPaymentHistory(Integer billId) {
        findById(billId); // throws if not found
        return paymentRepository.findByBillIdOrderByCreatedAtAsc(billId)
                .stream().map(this::toPaymentResponse).collect(Collectors.toList());
    }

    private BillPaymentResponse toPaymentResponse(BillPaymentEntity p) {
        BillPaymentResponse r = new BillPaymentResponse();
        r.setId(p.getId());
        r.setBillId(p.getBill().getId());
        r.setBillNumber(p.getBill().getBillNumber());
        r.setAmount(p.getAmount());
        r.setPaymentMode(p.getPaymentMode());
        r.setPaymentReference(p.getPaymentReference());
        r.setPaymentDate(p.getPaymentDate());
        r.setEntryType(p.getEntryType());
        r.setReversalReason(p.getReversalReason());
        r.setRecordedBy(p.getRecordedBy());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }

    // ── Cancel Bill (restores stock if already issued) ────────────────────────────
    @Transactional
    public CustomerBillResponse cancelBill(Integer id, String username) {
        CustomerBillEntity bill = findById(id);

        // B-M2: PARTIALLY_PAID bills cannot be cancelled — they must go through payment reversal first
        if (bill.getStatus() == BillStatus.PAID
                || bill.getStatus() == BillStatus.PARTIALLY_PAID
                || bill.getStatus() == BillStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot cancel a bill with status: " + bill.getStatus()
                    + ". Bills with payments must be reversed before cancellation.");
        }

        // If stock was already deducted, restore it by cancelling each sale record
        if (bill.isStockDeducted()) {
            for (CustomerBillItemEntity item : bill.getItems()) {
                if (item.getProduct() != null) {
                    ProductMasterEntity product = item.getProduct();
                    BigDecimal restored = product.getCurrentStock().add(item.getQuantity());
                    product.setCurrentStock(restored);
                    productMasterRepository.save(product);
                    log.info("Stock restored for product {} due to bill {} cancellation: +{}",
                            product.getId(), bill.getBillNumber(), item.getQuantity());
                }
            }
            bill.setStockDeducted(false);
        }

        bill.setStatus(BillStatus.CANCELLED);
        bill.setUpdatedBy(username);
        CustomerBillEntity saved = billRepository.save(bill);
        log.info("Bill {} CANCELLED by {}", saved.getBillNumber(), username);
        auditService.record("CustomerBill", String.valueOf(saved.getId()), "BILL_CANCELLED",
                username, "Bill# " + saved.getBillNumber() + ", Total: " + saved.getTotalAmount()
                        + ", PaidAmount: " + saved.getPaidAmount());
        return toResponse(saved);
    }

    // ── Get One ───────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CustomerBillResponse getById(Integer id) {
        return toResponse(findById(id));
    }

    // ── Get All (paginated, optional filters) ─────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<CustomerBillResponse> getAll(int page, int size, BillStatus status, Integer customerId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && customerId != null) {
            return billRepository.findByStatusAndCustomer_CustomerId(status, customerId, pageable)
                    .map(this::toResponse);
        } else if (status != null) {
            return billRepository.findByStatus(status, pageable).map(this::toResponse);
        } else if (customerId != null) {
            return billRepository.findByCustomer_CustomerId(customerId, pageable).map(this::toResponse);
        }
        return billRepository.findAll(pageable).map(this::toResponse);
    }

    // ── Delete (DRAFT only) ───────────────────────────────────────────────────────
    @Transactional
    public void deleteBill(Integer id, String username) {
        CustomerBillEntity bill = findById(id);
        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT bills can be deleted. Current status: " + bill.getStatus());
        }
        billRepository.delete(bill);
        log.info("Bill {} deleted by {}", bill.getBillNumber(), username);
    }

    // ── Get all currently overdue bills ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CustomerBillResponse> getOverdueBills() {
        return billRepository.findOverdueBills(LocalDate.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Auto-flag overdue bills ───────────────────────────────────────────────────
    @Transactional
    public void flagOverdueBills() {
        List<CustomerBillEntity> overdue = billRepository.findOverdueBills(LocalDate.now());
        overdue.forEach(bill -> {
            bill.setStatus(BillStatus.OVERDUE);
            billRepository.save(bill);
            log.info("Bill {} auto-flagged as OVERDUE", bill.getBillNumber());
        });
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private CustomerBillEntity findById(Integer id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + id));
    }

    private String generateBillNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = billRepository.countByBillNumberStartingWith(year);
        // B-M6: Zero-pad sequence to 5 digits (e.g. 2025-00001) for consistent sorting and display
        return String.format("%s-%05d", year, count + 1);
    }

    private List<CustomerBillItemEntity> buildBillItems(List<CustomerBillItemRequest> requests,
                                                        CustomerBillEntity bill,
                                                        boolean interstate) {
        List<CustomerBillItemEntity> items = new ArrayList<>();
        for (CustomerBillItemRequest req : requests) {
            CustomerBillItemEntity item = new CustomerBillItemEntity();
            item.setBill(bill);

            if (req.getProductMasterId() != null) {
                ProductMasterEntity product = productMasterRepository.findById(req.getProductMasterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + req.getProductMasterId()));
                item.setProduct(product);
            }

            item.setDescription(req.getDescription());
            item.setHsnCode(req.getHsnCode());
            item.setQuantity(req.getQuantity());
            item.setUnit(req.getUnit());
            item.setUnitPrice(req.getUnitPrice());

            BigDecimal discountPct = req.getDiscountPct() != null ? req.getDiscountPct() : BigDecimal.ZERO;
            BigDecimal gstPct = req.getGstPct() != null ? req.getGstPct() : BigDecimal.ZERO;

            // Validate GST % against Indian statutory rates (0, 5, 12, 18, 28)
            validateGstRate(gstPct, req.getDescription());

            item.setDiscountPct(discountPct);
            item.setGstPct(gstPct);

            computeItemTax(item, interstate);
            items.add(item);
        }
        return items;
    }

    private void validateGstRate(BigDecimal gstPct, String lineDescription) {
        java.util.Set<Integer> validRates = java.util.Set.of(0, 5, 12, 18, 28);
        int intRate = gstPct.setScale(0, java.math.RoundingMode.HALF_UP).intValue();
        if (!validRates.contains(intRate)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid GST rate %.0f%% for line item '%s'. Valid rates are: 0, 5, 12, 18, 28.",
                    gstPct, lineDescription != null ? lineDescription : "?"));
        }
    }

    /**
     * taxableAmount = (qty × unitPrice) − discountAmount
     * Intrastate: cgst = taxable × gstPct/200, sgst = taxable × gstPct/200
     * Interstate:  igst = taxable × gstPct/100
     * lineTotal = taxableAmount + cgst + sgst  OR  taxableAmount + igst
     */
    private void computeItemTax(CustomerBillItemEntity item, boolean interstate) {
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

    private void calculateBillTotals(CustomerBillEntity bill) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CustomerBillItemEntity item : bill.getItems()) {
            subtotal = subtotal.add(item.getQuantity().multiply(item.getUnitPrice()));
            totalDiscount = totalDiscount.add(item.getDiscountAmount());
            totalCgst = totalCgst.add(item.getCgstAmount());
            totalSgst = totalSgst.add(item.getSgstAmount());
            totalIgst = totalIgst.add(item.getIgstAmount());
            totalAmount = totalAmount.add(item.getLineTotal());
        }

        bill.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalCgst(totalCgst.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalSgst(totalSgst.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalIgst(totalIgst.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setBalanceAmount(totalAmount.subtract(bill.getPaidAmount()).setScale(2, RoundingMode.HALF_UP));
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
    private CustomerBillResponse toResponse(CustomerBillEntity b) {
        CustomerBillResponse r = new CustomerBillResponse();
        r.setId(b.getId());
        r.setBillNumber(b.getBillNumber());
        r.setQuotationId(b.getQuotation() != null ? b.getQuotation().getId() : null);
        r.setCustomerId(b.getCustomer().getCustomerId());
        r.setCustomerName(b.getCustomer().getCustomerName());
        r.setCustomerSnapshot(b.getCustomerSnapshot());
        r.setBillDate(b.getBillDate());
        r.setDueDate(b.getDueDate());
        r.setStatus(b.getStatus());
        r.setInterstate(b.isInterstate());
        r.setSubtotal(b.getSubtotal());
        r.setTotalDiscount(b.getTotalDiscount());
        r.setTotalCgst(b.getTotalCgst());
        r.setTotalSgst(b.getTotalSgst());
        r.setTotalIgst(b.getTotalIgst());
        r.setTotalAmount(b.getTotalAmount());
        r.setPaidAmount(b.getPaidAmount());
        r.setBalanceAmount(b.getBalanceAmount());
        r.setPaymentMode(b.getPaymentMode());
        r.setPaymentReference(b.getPaymentReference());
        r.setStockDeducted(b.isStockDeducted());
        r.setNotes(b.getNotes());
        r.setTerms(b.getTerms());
        r.setCreatedBy(b.getCreatedBy());
        r.setCreatedAt(b.getCreatedAt());
        r.setUpdatedAt(b.getUpdatedAt());

        List<CustomerBillItemResponse> itemResponses = b.getItems().stream().map(item -> {
            CustomerBillItemResponse ir = new CustomerBillItemResponse();
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
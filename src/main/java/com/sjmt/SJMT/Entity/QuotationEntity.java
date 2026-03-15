package com.sjmt.SJMT.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
public class QuotationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quotation_number", nullable = false, unique = true, length = 20)
    private String quotationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    // JSON snapshot of customer details at the time of creation
    @Column(name = "customer_snapshot", columnDefinition = "TEXT")
    private String customerSnapshot;

    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuotationStatus status = QuotationStatus.DRAFT;

    // true = interstate (IGST), false = intrastate (CGST + SGST)
    @Column(name = "is_interstate", nullable = false)
    private boolean interstate = false;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuotationItemEntity> items = new ArrayList<>();

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "total_discount", precision = 15, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "total_cgst", precision = 15, scale = 2)
    private BigDecimal totalCgst = BigDecimal.ZERO;

    @Column(name = "total_sgst", precision = 15, scale = 2)
    private BigDecimal totalSgst = BigDecimal.ZERO;

    @Column(name = "total_igst", precision = 15, scale = 2)
    private BigDecimal totalIgst = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public QuotationEntity() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }

    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }

    public String getCustomerSnapshot() { return customerSnapshot; }
    public void setCustomerSnapshot(String customerSnapshot) { this.customerSnapshot = customerSnapshot; }

    public LocalDate getQuotationDate() { return quotationDate; }
    public void setQuotationDate(LocalDate quotationDate) { this.quotationDate = quotationDate; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public QuotationStatus getStatus() { return status; }
    public void setStatus(QuotationStatus status) { this.status = status; }

    public boolean isInterstate() { return interstate; }
    public void setInterstate(boolean interstate) { this.interstate = interstate; }

    public List<QuotationItemEntity> getItems() { return items; }
    public void setItems(List<QuotationItemEntity> items) { this.items = items; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }

    public BigDecimal getTotalCgst() { return totalCgst; }
    public void setTotalCgst(BigDecimal totalCgst) { this.totalCgst = totalCgst; }

    public BigDecimal getTotalSgst() { return totalSgst; }
    public void setTotalSgst(BigDecimal totalSgst) { this.totalSgst = totalSgst; }

    public BigDecimal getTotalIgst() { return totalIgst; }
    public void setTotalIgst(BigDecimal totalIgst) { this.totalIgst = totalIgst; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
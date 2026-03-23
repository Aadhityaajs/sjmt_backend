package com.sjmt.SJMT.DTO.ResponseDTO;

import com.sjmt.SJMT.Entity.QuotationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class QuotationResponse {

    private Integer id;
    private String quotationNumber;
    private Integer customerId;
    private String customerName;
    private String customerSnapshot;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private QuotationStatus status;
    private boolean interstate;
    private List<QuotationItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalAmount;
    private String notes;
    private String terms;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

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

    public List<QuotationItemResponse> getItems() { return items; }
    public void setItems(List<QuotationItemResponse> items) { this.items = items; }

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
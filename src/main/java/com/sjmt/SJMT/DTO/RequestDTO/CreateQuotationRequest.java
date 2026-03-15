package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CreateQuotationRequest {

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "Quotation date is required")
    private LocalDate quotationDate;

    @NotNull(message = "Valid until date is required")
    private LocalDate validUntil;

    // true = interstate (IGST), false = intrastate (CGST + SGST)
    private boolean interstate = false;

    @NotNull(message = "At least one item is required")
    @Valid
    private List<QuotationItemRequest> items;

    private String notes;

    private String terms;

    // Getters and Setters
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public LocalDate getQuotationDate() { return quotationDate; }
    public void setQuotationDate(LocalDate quotationDate) { this.quotationDate = quotationDate; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public boolean isInterstate() { return interstate; }
    public void setInterstate(boolean interstate) { this.interstate = interstate; }

    public List<QuotationItemRequest> getItems() { return items; }
    public void setItems(List<QuotationItemRequest> items) { this.items = items; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
}
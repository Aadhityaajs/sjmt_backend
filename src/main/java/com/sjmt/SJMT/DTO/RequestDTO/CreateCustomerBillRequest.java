package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CreateCustomerBillRequest {

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "Bill date is required")
    private LocalDate billDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    // true = interstate (IGST), false = intrastate (CGST + SGST)
    private boolean interstate = false;

    @NotNull(message = "At least one item is required")
    @Valid
    private List<CustomerBillItemRequest> items;

    private String notes;

    private String terms;

    // Getters and Setters
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isInterstate() { return interstate; }
    public void setInterstate(boolean interstate) { this.interstate = interstate; }

    public List<CustomerBillItemRequest> getItems() { return items; }
    public void setItems(List<CustomerBillItemRequest> items) { this.items = items; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
}
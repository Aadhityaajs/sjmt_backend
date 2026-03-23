package com.sjmt.SJMT.Entity;

public enum QuotationStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    CONVERTED,
    /** Quotation forcibly closed by the system, e.g. customer blacklisted */
    CANCELLED
}
package com.sjmt.SJMT.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "quotation_items")
public class QuotationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private QuotationEntity quotation;

    // Nullable — allows free-text/custom items without a product master entry
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_master_id", nullable = true)
    private ProductMasterEntity product;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "hsn_code", length = 8)
    private String hsnCode;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_pct", precision = 5, scale = 2)
    private BigDecimal discountPct = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gst_pct", precision = 5, scale = 2)
    private BigDecimal gstPct = BigDecimal.ZERO;

    @Column(name = "cgst_amount", precision = 15, scale = 2)
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 15, scale = 2)
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount", precision = 15, scale = 2)
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "line_total", precision = 15, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public QuotationItemEntity() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public QuotationEntity getQuotation() { return quotation; }
    public void setQuotation(QuotationEntity quotation) { this.quotation = quotation; }

    public ProductMasterEntity getProduct() { return product; }
    public void setProduct(ProductMasterEntity product) { this.product = product; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscountPct() { return discountPct; }
    public void setDiscountPct(BigDecimal discountPct) { this.discountPct = discountPct; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getGstPct() { return gstPct; }
    public void setGstPct(BigDecimal gstPct) { this.gstPct = gstPct; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
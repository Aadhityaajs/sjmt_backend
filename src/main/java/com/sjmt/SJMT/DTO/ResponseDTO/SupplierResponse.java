package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.SupplierStatusEnum;

/**
 * Response DTO for Supplier data
 * @author SJMT Team
 */
public class SupplierResponse {

    private Integer supplierId;
    private String supplierName;
    private String supplierEmail;
    private String phoneNumber;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
    private int pincode;
    private LocalDateTime createdAt;
    private SupplierStatusEnum status;

    // Getters and Setters
    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierEmail() { return supplierEmail; }
    public void setSupplierEmail(String supplierEmail) { this.supplierEmail = supplierEmail; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getPincode() { return pincode; }
    public void setPincode(int pincode) { this.pincode = pincode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public SupplierStatusEnum getStatus() { return status; }
    public void setStatus(SupplierStatusEnum status) { this.status = status; }
}
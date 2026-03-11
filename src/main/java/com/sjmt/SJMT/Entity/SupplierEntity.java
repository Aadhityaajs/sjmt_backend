package com.sjmt.SJMT.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "suppliers")
public class SupplierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Integer supplierId;

    @NotBlank(message = "supplier name is required")
    @Column(name = "supplier_name", nullable = false, unique = true, length = 50)
    private String supplierName;

    @NotBlank(message = "supplier Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "supplier_email", nullable = false, unique = true, length = 100)
    private String supplierEmail;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @Column(name = "gst_number", nullable = true, updatable = true)
    private String gstNumber = null;


    @Column(name = "address", nullable = false, updatable = true)
    private String address;

    @Column(name = "city", nullable = false, updatable = true)
    private String city;

    @Column(name = "state", nullable = false, updatable = true)
    private String state;

    @Column(name = "pincode" ,nullable = false, updatable = true)
    private int pincode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "status", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private SupplierStatusEnum status = SupplierStatusEnum.WHITELISTED;

    public Integer getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPincode() {
        return pincode;
    }

    public void setPincode(int pincode) {
        this.pincode = pincode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public SupplierStatusEnum getStatus() {
        return status;
    }

    public void setStatus(SupplierStatusEnum status) {
        this.status = status;
    }

    public SupplierEntity() {
    }

    public SupplierEntity(String supplierName, String supplierEmail, String phoneNumber, String gstNumber, String address, String city, String state, int pincode, LocalDateTime createdAt, SupplierStatusEnum status) {
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.phoneNumber = phoneNumber;
        this.gstNumber = gstNumber;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.createdAt = createdAt;
        this.status = status;
    }
}

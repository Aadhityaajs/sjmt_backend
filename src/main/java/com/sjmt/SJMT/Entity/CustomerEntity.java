package com.sjmt.SJMT.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @NotBlank(message = "customer name is required")
    @Column(name = "customer_name", nullable = false, unique = true, length = 50)
    private String customerName;

    @NotBlank(message = "customer Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "customer_email", nullable = false, unique = true, length = 100)
    private String customerEmail;

    @Column(name = "phone_number", length = 20)
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

    @Column(name = "status", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private CustomerStatusEnum status = CustomerStatusEnum.WHITELISTED;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName){
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public CustomerStatusEnum getStatus() {
        return status;
    }

    public void setStatus(CustomerStatusEnum status) {
        this.status = status;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public CustomerEntity() {
    }

    public CustomerEntity(String customerName, String customerEmail, String phoneNumber, String gstNumber, String address, String city, String state, int pincode, LocalDateTime createdAt, CustomerStatusEnum status) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
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

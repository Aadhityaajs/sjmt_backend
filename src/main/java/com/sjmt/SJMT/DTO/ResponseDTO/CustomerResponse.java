package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.CustomerStatusEnum;

public class CustomerResponse {
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private String phoneNumber;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
    private int pincode;
    private LocalDateTime createdAt;
    private CustomerStatusEnum status;

    // Getters and Setters
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
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
    public CustomerStatusEnum getStatus() { return status; }
    public void setStatus(CustomerStatusEnum status) { this.status = status; }
}
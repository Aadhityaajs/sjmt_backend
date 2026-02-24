package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Update Profile Request DTO
 */
public class UpdateProfileRequest {
    
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone number must be valid (10-20 digits)")
    private String phoneNumber;
    
    public UpdateProfileRequest() {
    }
    
    public UpdateProfileRequest(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
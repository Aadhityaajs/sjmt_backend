package com.sjmt.SJMT.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for Creating/Updating a Supplier
 * @author SJMT Team
 */
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(min = 2, max = 50, message = "Supplier name must be between 2 and 50 characters")
    @Schema(example = "Global Logistics Ltd")
    private String supplierName;

    @NotBlank(message = "Supplier email is required")
    @Email(message = "Email must be valid")
    @Schema(example = "contact@globallogistics.com")
    private String supplierEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 to 15 digits")
    @Schema(example = "9876543210")
    private String phoneNumber;

    @Schema(example = "22AAAAA0000A1Z5")
    private String gstNumber;

    @NotBlank(message = "Address is required")
    @Schema(example = "123 Industrial Area")
    private String address;

    @NotBlank(message = "City is required")
    @Schema(example = "Mumbai")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(example = "Maharashtra")
    private String state;

    @NotNull(message = "Pincode is required")
    @Schema(example = "400001")
    private Integer pincode;

    // Getters and Setters
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

    public Integer getPincode() { return pincode; }
    public void setPincode(Integer pincode) { this.pincode = pincode; }
}
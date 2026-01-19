package com.sjmt.SJMT.DTO.RequestDTO;

import com.sjmt.SJMT.Entity.PrivilegesEnum;
import com.sjmt.SJMT.Entity.UserRoleEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Create User Request DTO (Used by Admin)
 * @author SJMT Team
 * @version 1.0
 */
public class CreateUserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be valid (10)")
    private String phoneNumber;
    
    @NotNull(message = "Role is required")
    private UserRoleEnum role = UserRoleEnum.STAFF;
    
    @NotNull(message = "Privileges are required")
    private PrivilegesEnum privileges = PrivilegesEnum.READ;
    
    // Constructors
    public CreateUserRequest() {
    }
    
    public CreateUserRequest(String username, String email, String fullName, 
                           String phoneNumber, UserRoleEnum role, PrivilegesEnum privileges) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    public UserRoleEnum getRole() {
        return role;
    }
    
    public void setRole(UserRoleEnum role) {
        this.role = role;
    }
    
    public PrivilegesEnum getPrivileges() {
        return privileges;
    }
    
    public void setPrivileges(PrivilegesEnum privileges) {
        this.privileges = privileges;
    }
}
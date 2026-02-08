package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.PrivilegesEnum;
import com.sjmt.SJMT.Entity.UserRoleEnum;
import com.sjmt.SJMT.Entity.UserStatusEnum;

/**
 * User Response DTO
 * @author SJMT Team
 * @version 1.0
 */
public class UserResponse {
    
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean emailVerified;
    private UserRoleEnum role;
    private UserStatusEnum status;
    private PrivilegesEnum privileges;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Constructors
    public UserResponse() {
    }
    
    public UserResponse(Integer userId, String username, String email, String fullName,
                       String phoneNumber, boolean emailVerified, UserRoleEnum role,
                       UserStatusEnum status, PrivilegesEnum privileges,
                       LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.role = role;
        this.status = status;
        this.privileges = privileges;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
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
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public UserRoleEnum getRole() {
        return role;
    }
    
    public void setRole(UserRoleEnum role) {
        this.role = role;
    }
    
    public UserStatusEnum getStatus() {
        return status;
    }
    
    public void setStatus(UserStatusEnum status) {
        this.status = status;
    }
    
    public PrivilegesEnum getPrivileges() {
        return privileges;
    }
    
    public void setPrivileges(PrivilegesEnum privileges) {
        this.privileges = privileges;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
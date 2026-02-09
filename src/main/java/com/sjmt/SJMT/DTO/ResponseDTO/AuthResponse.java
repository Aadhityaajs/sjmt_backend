package com.sjmt.SJMT.DTO.ResponseDTO;

import com.sjmt.SJMT.Entity.PrivilegesEnum;
import com.sjmt.SJMT.Entity.UserRoleEnum;

/**
 * Authentication Response DTO
 * @author SJMT Team
 * @version 1.0
 */
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private UserRoleEnum role;
    private PrivilegesEnum privileges;
    private Long expiresIn;
    private boolean requirePasswordChange; 
    
    
    // Constructors
    public AuthResponse() {
    }
    
    public AuthResponse(String accessToken, String refreshToken, Integer userId, 
                       String username, String email, String fullName,
                       UserRoleEnum role, PrivilegesEnum privileges, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.privileges = privileges;
        this.expiresIn = expiresIn;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
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
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public boolean isRequirePasswordChange() {
        return requirePasswordChange;
    }

    public void setRequirePasswordChange(boolean requirePasswordChange) {
        this.requirePasswordChange = requirePasswordChange;
    }
}
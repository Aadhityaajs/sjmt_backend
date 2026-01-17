package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * @author SJMT Team
 * @version 1.0
 */
public class LoginRequest {
    
    @NotBlank(message = "Username or email is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    // Constructors
    public LoginRequest() {
    }
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
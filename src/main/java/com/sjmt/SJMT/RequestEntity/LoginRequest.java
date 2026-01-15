package com.sjmt.SJMT.RequestEntity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;


public class LoginRequest {

    @NotBlank(message = "username/Email required")
    private String usernameEmail;

    @NotBlank(message = "password required")
    private String password;

    public String getUsernameEmail() {
        return usernameEmail;
    }

    public void setUsernameEmail(String usernameEmail) {
        this.usernameEmail = usernameEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginRequest(String usernameEmail, String password) {
        this.usernameEmail = usernameEmail;
        this.password = password;
    }
}

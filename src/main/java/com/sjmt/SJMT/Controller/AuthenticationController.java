package com.sjmt.SJMT.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.AuthResponse;
import com.sjmt.SJMT.DTO.RequestDTO.ChangePasswordRequest;
import com.sjmt.SJMT.DTO.RequestDTO.ForgotPasswordRequest;
import com.sjmt.SJMT.DTO.RequestDTO.LoginRequest;
import com.sjmt.SJMT.DTO.RequestDTO.RefreshTokenRequest;
import com.sjmt.SJMT.DTO.RequestDTO.ResetPasswordRequest;
import com.sjmt.SJMT.Service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Authentication Controller
 * @author SJMT Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * Login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.info("Login request received for: {}", request.getUsername());
            AuthResponse response = authenticationService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            logger.info("Refresh token request received");
            AuthResponse response = authenticationService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "User logout", description = "Logout user and revoke tokens")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Logout request received for: {}", username);
            authenticationService.logout(username);
            return ResponseEntity.ok(ApiResponse.success("Logout successful"));
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Verify email endpoint
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        try {
            logger.info("Email verification request received");
            authenticationService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
        } catch (Exception e) {
            logger.error("Email verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Forgot password endpoint
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            logger.info("Forgot password request received for: {}", request.getEmail());
            authenticationService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Password reset link sent to your email"));
        } catch (Exception e) {
            logger.error("Forgot password failed: {}", e.getMessage());
            // Return success message even if email doesn't exist (security best practice)
            return ResponseEntity.ok(ApiResponse.success("If the email exists, a password reset link has been sent"));
        }
    }
    
    /**
     * Reset password endpoint
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            logger.info("Password reset request received");
            authenticationService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password reset successful. Please login with your new password."));
        } catch (Exception e) {
            logger.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Change password endpoint (authenticated users)
     */
    @PostMapping("/change-password")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Password change request received for: {}", username);
            authenticationService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully. Please login again."));
        } catch (Exception e) {
            logger.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
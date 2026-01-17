package com.sjmt.SJMT.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateProfileRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.UserResponse;
import com.sjmt.SJMT.Service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * User Controller
 * @author SJMT Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User", description = "User management APIs")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Get user profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Get profile request for user: {}", username);
            UserResponse response = userService.getUserProfile(username);
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Get profile failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update current user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Update profile request for user: {}", username);
            UserResponse response = userService.updateUserProfile(
                username, 
                request.getFullName(), 
                request.getPhoneNumber()
            );
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
        } catch (Exception e) {
            logger.error("Update profile failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
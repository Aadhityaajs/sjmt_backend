package com.sjmt.SJMT.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotNull;

import com.sjmt.SJMT.DTO.RequestDTO.CreateUserRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.UserResponse;
import com.sjmt.SJMT.Entity.PrivilegesEnum;
import com.sjmt.SJMT.Entity.UserRoleEnum;
import com.sjmt.SJMT.Entity.UserStatusEnum;
import com.sjmt.SJMT.Service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Admin Controller
 * @author SJMT Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Create new user (Staff or Admin)
     */
    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create new user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            logger.info("Create user request for: {}", request.getUsername());
            UserResponse response = userService.addNewUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully. Verification email sent.", response));
        } catch (Exception e) {
            logger.error("Create user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get all users
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Get list of all users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            logger.info("Get all users request");
            List<UserResponse> response = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Get all users failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        try {
            logger.info("Get user by ID request: {}", id);
            UserResponse response = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Get user by ID failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update user role and privileges
     */
    @PutMapping("/users/{id}/role-privileges")
    @Operation(summary = "Update user role and privileges", description = "Update user role and privileges (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoleAndPrivileges(
            @PathVariable Integer id,
            @RequestBody UpdateRolePrivilegesRequest request,
            Authentication authentication) {
        try {
            logger.info("Update role and privileges request for user ID: {}", id);
            UserResponse response = userService.updateUserRoleAndPrivileges(
                id, 
                request.getRole(), 
                request.getPrivileges(),
                authentication.getName()
            );
            return ResponseEntity.ok(ApiResponse.success("User role and privileges updated successfully", response));
        } catch (Exception e) {
            logger.error("Update role and privileges failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update user status (Active/Blocked)
     */
    @PutMapping("/users/{id}/status")
    @Operation(summary = "Update user status", description = "Activate or block user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Integer id,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication) {
        try {
            logger.info("Update status request for user ID: {}", id);
            UserResponse response = userService.updateUserStatus(id, request.getStatus(), authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
        } catch (Exception e) {
            logger.error("Update status failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user", description = "Delete user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id, Authentication authentication) {
        try {
            logger.info("Delete user request for ID: {}", id);
            userService.deleteUser(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            logger.error("Delete user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Reset user password
     */
    @PostMapping("/users/{id}/reset-password")
    @Operation(summary = "Reset user password", description = "Send password reset email to user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable Integer id) {
        try {
            logger.info("Reset password request for user ID: {}", id);
            userService.resetUserPassword(id);
            return ResponseEntity.ok(ApiResponse.success("Password reset email sent successfully"));
        } catch (Exception e) {
            logger.error("Reset password failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Inner class for update role and privileges request
     */
    public static class UpdateRolePrivilegesRequest {
        @NotNull(message = "Role is required")
        private UserRoleEnum role;
        private PrivilegesEnum privileges;
        
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
    
    /**
     * Inner class for update status request
     */
    public static class UpdateStatusRequest {
        private UserStatusEnum status;
        
        public UserStatusEnum getStatus() {
            return status;
        }
        
        public void setStatus(UserStatusEnum status) {
            this.status = status;
        }
    }
}
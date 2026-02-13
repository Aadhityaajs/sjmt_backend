package com.sjmt.SJMT.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.Config.TemporaryPassword;
import com.sjmt.SJMT.DTO.RequestDTO.CreateUserRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.UserResponse;
import com.sjmt.SJMT.Entity.EmailVerificationTokenEntity;
import com.sjmt.SJMT.Entity.PrivilegesEnum;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Entity.UserRoleEnum;
import com.sjmt.SJMT.Entity.UserStatusEnum;
import com.sjmt.SJMT.Repository.UserRepository;

/**
 * User Service
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private TemporaryPassword temporaryPassword;
    
    /**
     * Create new user (by Admin)
     */
    @Transactional
    public UserResponse addNewUser(CreateUserRequest request) {
        logger.info("Creating new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // CRITICAL: ADMIN role must ALWAYS have UPDATE privilege
        UserRoleEnum role = request.getRole();
        PrivilegesEnum privileges = request.getPrivileges();
        
        if (role == UserRoleEnum.ADMIN) {
            if (privileges != PrivilegesEnum.UPDATE) {
                throw new RuntimeException("ADMIN role must have UPDATE privilege. Cannot create ADMIN with other privileges.");
            }
            privileges = PrivilegesEnum.UPDATE; // Force UPDATE for ADMIN
            logger.info("ADMIN role detected - enforcing UPDATE privilege");
        }
        
        // Create user entity with temporary password
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);
        user.setPrivileges(privileges);
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setEmailVerified(false);
        
        // Set temporary password (will be changed during email verification)
        // String tempPassword = UUID.randomUUID().toString();
        // user.setPassword(passwordEncoder.encode(tempPassword));
        
        // Save user
        UserEntity savedUser = userRepository.save(user);
        
        // Create email verification token
        EmailVerificationTokenEntity token = tokenService.createEmailVerificationToken(savedUser);
        
        // Send email with set password link
        emailService.sendEmailVerification(savedUser.getEmail(), token.getToken(), savedUser.getUsername());
        
        logger.info("User created successfully: {}", savedUser.getUsername());
        
        return convertToUserResponse(savedUser);
    }
    
    /**
     * Get user profile
     */
    public UserResponse getUserProfile(String username) {
        logger.info("Fetching profile for user: {}", username);
        
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return convertToUserResponse(user);
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUserProfile(String username, String fullName, String phoneNumber) {
        logger.info("Updating profile for user: {}", username);
        
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }
        
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            user.setPhoneNumber(phoneNumber);
        }
        
        UserEntity updatedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", username);
        
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Get all users (Admin only)
     */
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToUserResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID (Admin only)
     */
    public UserResponse getUserById(Integer userId) {
        logger.info("Fetching user by ID: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        return convertToUserResponse(user);
    }
    
    /**
     * Update user role and privileges (Admin only)
     */
    @Transactional
    public UserResponse updateUserRoleAndPrivileges(Integer userId, UserRoleEnum role, PrivilegesEnum privileges) {
        logger.info("Updating role and privileges for user ID: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // CRITICAL: ADMIN role must ALWAYS have UPDATE privilege
        if (role == UserRoleEnum.ADMIN && privileges != PrivilegesEnum.UPDATE) {
            throw new RuntimeException("ADMIN role must have UPDATE privilege. Cannot change ADMIN privileges.");
        }
        
        // If changing existing ADMIN to STAFF, allow it
        // If setting role to ADMIN, force UPDATE privilege
        if (role == UserRoleEnum.ADMIN) {
            privileges = PrivilegesEnum.UPDATE;
            logger.info("ADMIN role detected - enforcing UPDATE privilege");
        }
        
        user.setRole(role);
        user.setPrivileges(privileges);
        
        UserEntity updatedUser = userRepository.save(user);
        logger.info("Role and privileges updated for user: {}", user.getUsername());
        
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Update user status (Admin only)
     */
    @Transactional
    public UserResponse updateUserStatus(Integer userId, UserStatusEnum status) {
        logger.info("Updating status for user ID: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setStatus(status);
        
        // If blocking user, revoke all tokens
        if (status == UserStatusEnum.BLOCKED) {
            tokenService.revokeAllUserTokens(user);
        }
        
        UserEntity updatedUser = userRepository.save(user);
        logger.info("Status updated for user: {}", user.getUsername());
        
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Delete user (Admin only)
     */
    @Transactional
    public void deleteUser(Integer userId) {
        logger.info("Deleting user with ID: {}", userId);        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Delete all user tokens
        tokenService.deleteUserTokens(user);
        
        // Delete user
        user.setStatus(UserStatusEnum.DELETED); // Requirement: Delete is soft delete
        
        logger.info("User deleted successfully: {}", user.getUsername());        
    }
    
    /**
     * Reset user password (Admin only)
     */
    @Transactional
    public void resetUserPassword(Integer userId) {
        logger.info("Resetting password for user ID: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // // Create password reset token
        // tokenService.createPasswordResetToken(user);

        // Send temporary password email
            String tempPassword = temporaryPassword.generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setTemporaryPassword(true);
            user.setTempPasswordPlain(tempPassword);
            userRepository.save(user);
        
        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), tempPassword, user.getUsername());
        
        logger.info("Password reset email sent to user: {}", user.getUsername());
    }
    
    /**
     * Convert UserEntity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(UserEntity user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmailVerified(user.isEmailVerified());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setPrivileges(user.getPrivileges());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLogin(user.getLastLogin());
        return response;
    }
}
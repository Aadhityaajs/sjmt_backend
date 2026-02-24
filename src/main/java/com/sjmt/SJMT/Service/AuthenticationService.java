package com.sjmt.SJMT.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.Config.TemporaryPassword;
import com.sjmt.SJMT.DTO.RequestDTO.LoginRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.AuthResponse;
import com.sjmt.SJMT.Entity.EmailVerificationTokenEntity;
import com.sjmt.SJMT.Entity.PasswordResetTokenEntity;
import com.sjmt.SJMT.Entity.RefreshTokenEntity;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Repository.UserRepository;
import com.sjmt.SJMT.Security.CustomUserDetailsService;
import com.sjmt.SJMT.Security.JwtUtil;


/**
 * Authentication Service
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TemporaryPassword temporaryPassword;
    
    /**
     * Login user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for: {}", request.getUsername());
        
//         Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
//
//        // Get user entity
        UserEntity user = userRepository.findByUsernameOrEmail(
            request.getUsername(),
            request.getUsername()
        ).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        // Save refresh token
        tokenService.createRefreshToken(user, refreshToken);
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        logger.info("User logged in successfully: {}", user.getUsername());
        
        // Create response
        return new AuthResponse(
            accessToken,
            refreshToken,
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getPrivileges(),
            jwtUtil.getAccessTokenExpiration()
        );
    }
    
    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        logger.info("Refresh token request received");
        
        // Verify refresh token
        RefreshTokenEntity refreshToken = tokenService.verifyRefreshToken(refreshTokenStr);
        UserEntity user = refreshToken.getUser();
        
        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        
        logger.info("Access token refreshed for user: {}", user.getUsername());
        
        // Create response
        return new AuthResponse(
            newAccessToken,
            refreshTokenStr,
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getPrivileges(),
            jwtUtil.getAccessTokenExpiration()
        );
    }
    
    /**
     * Logout user and revoke tokens
     */
    @Transactional
    public void logout(String username) {
        logger.info("Logout request for user: {}", username);
        
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Revoke all refresh tokens
        tokenService.revokeAllUserTokens(user);
        
        SecurityContextHolder.clearContext();
        logger.info("User logged out successfully: {}", username);
    }
    
    /**
     * Verify email with token
     */
    @Transactional
    public void verifyEmail(String token) {
        logger.info("Email verification attempt with token");
        
        // Verify token
        EmailVerificationTokenEntity verificationToken = tokenService.verifyEmailVerificationToken(token);
        UserEntity user = verificationToken.getUser();
        
        // Mark email as verified
        user.setEmailVerified(true);
        userRepository.save(user);
        
        // Mark token as used
        tokenService.markEmailVerificationTokenAsUsed(verificationToken);

        boolean used_token = verificationToken.isUsed();

        if(used_token && user.isEmailVerified()){ 
            logger.info("Email verified successfully for user: {}", user.getUsername());

            // Send temporary password email
            String tempPassword = temporaryPassword.generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setTemporaryPassword(true);
            user.setTempPasswordPlain(tempPassword);
            userRepository.save(user);

            emailService.sendTemporaryPasswordEmail(user.getEmail(), tempPassword, user.getUsername());

            logger.info("Temporary password has been sent to user: {}", user.getUsername());

        } 
        
        
    }
    
    /**
     * Request password reset
     */
    @Transactional
    public void forgotPassword(String email) {
        logger.info("Password reset request for email: {}", email);
        
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Create password reset token
        PasswordResetTokenEntity resetToken = tokenService.createPasswordResetToken(user);
        
        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken(), user.getUsername());
        
        logger.info("Password reset email sent to: {}", email);
    }
    
    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        logger.info("Password reset attempt with token");
        
        // Verify token
        PasswordResetTokenEntity resetToken = tokenService.verifyPasswordResetToken(token);
        UserEntity user = resetToken.getUser();
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        tokenService.markPasswordResetTokenAsUsed(resetToken);
        
        // Revoke all existing sessions
        tokenService.revokeAllUserTokens(user);
        
        logger.info("Password reset successfully for user: {}", user.getUsername());
    }
    
    /**
     * Change password (authenticated user)
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        logger.info("Password change request for user: {}", username);
        
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Revoke all existing sessions
        tokenService.revokeAllUserTokens(user);
        
        logger.info("Password changed successfully for user: {}", username);
    }

    

}
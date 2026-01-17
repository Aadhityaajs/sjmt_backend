package com.sjmt.SJMT.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.Entity.EmailVerificationTokenEntity;
import com.sjmt.SJMT.Entity.PasswordResetTokenEntity;
import com.sjmt.SJMT.Entity.RefreshTokenEntity;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Repository.EmailVerificationTokenRepository;
import com.sjmt.SJMT.Repository.PasswordResetTokenRepository;
import com.sjmt.SJMT.Repository.RefreshTokenRepository;

/**
 * Token Service for managing all types of tokens
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class TokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;
    
    @Value("${app.email-verification-token.expiration}")
    private Long emailVerificationTokenExpiration;
    
    @Value("${app.password-reset-token.expiration}")
    private Long passwordResetTokenExpiration;
    
    /**
     * Create and save refresh token
     */
    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user, String token) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshToken.setCreatedAt(LocalDateTime.now());
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Verify refresh token
     */
    public RefreshTokenEntity verifyRefreshToken(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token is expired");
        }
        
        return refreshToken;
    }
    
    /**
     * Revoke all user's refresh tokens
     */
    @Transactional
    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        logger.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }
    
    /**
     * Delete user's refresh tokens
     */
    @Transactional
    public void deleteUserTokens(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
        logger.info("Deleted all refresh tokens for user: {}", user.getUsername());
    }
    
    /**
     * Create email verification token
     */
    @Transactional
    public EmailVerificationTokenEntity createEmailVerificationToken(UserEntity user) {
        // Delete existing token if any
        emailVerificationTokenRepository.findByUser(user)
            .ifPresent(emailVerificationTokenRepository::delete);
        
        String token = UUID.randomUUID().toString();
        EmailVerificationTokenEntity verificationToken = new EmailVerificationTokenEntity();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusSeconds(emailVerificationTokenExpiration / 1000));
        verificationToken.setCreatedAt(LocalDateTime.now());
        
        return emailVerificationTokenRepository.save(verificationToken);
    }
    
    /**
     * Verify email verification token
     */
    public EmailVerificationTokenEntity verifyEmailVerificationToken(String token) {
        EmailVerificationTokenEntity verificationToken = emailVerificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (verificationToken.isUsed()) {
            throw new RuntimeException("Verification token has already been used");
        }
        
        if (verificationToken.isExpired()) {
            throw new RuntimeException("Verification token has expired");
        }
        
        return verificationToken;
    }
    
    /**
     * Mark email verification token as used
     */
    @Transactional
    public void markEmailVerificationTokenAsUsed(EmailVerificationTokenEntity token) {
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }
    
    /**
     * Create password reset token
     */
    @Transactional
    public PasswordResetTokenEntity createPasswordResetToken(UserEntity user) {
        // Delete existing tokens
        passwordResetTokenRepository.deleteByUser(user);
        
        String token = UUID.randomUUID().toString();
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusSeconds(passwordResetTokenExpiration / 1000));
        resetToken.setCreatedAt(LocalDateTime.now());
        
        return passwordResetTokenRepository.save(resetToken);
    }
    
    /**
     * Verify password reset token
     */
    public PasswordResetTokenEntity verifyPasswordResetToken(String token) {
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        
        if (resetToken.isUsed()) {
            throw new RuntimeException("Password reset token has already been used");
        }
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }
        
        return resetToken;
    }
    
    /**
     * Mark password reset token as used
     */
    @Transactional
    public void markPasswordResetTokenAsUsed(PasswordResetTokenEntity token) {
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }
    
    /**
     * Clean up expired tokens (can be scheduled)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        emailVerificationTokenRepository.deleteExpiredTokens(now);
        passwordResetTokenRepository.deleteExpiredTokens(now);
        logger.info("Cleaned up expired tokens");
    }
}
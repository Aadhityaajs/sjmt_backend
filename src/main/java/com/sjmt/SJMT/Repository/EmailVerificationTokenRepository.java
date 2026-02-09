package com.sjmt.SJMT.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.EmailVerificationTokenEntity;
import com.sjmt.SJMT.Entity.UserEntity;

/**
 * Email Verification Token Repository
 * @author SJMT Team
 * @version 1.0
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Integer> {
    
    Optional<EmailVerificationTokenEntity> findByToken(String token);
    
    Optional<EmailVerificationTokenEntity> findByUser(UserEntity user);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity evt WHERE evt.expiryDate < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity evt WHERE evt.user = ?1")
    void deleteByUser(UserEntity user);
}
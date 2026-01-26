package com.sjmt.SJMT.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.PasswordResetTokenEntity;
import com.sjmt.SJMT.Entity.UserEntity;

/**
 * Password Reset Token Repository
 * @author SJMT Team
 * @version 1.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Integer> {
    
    Optional<PasswordResetTokenEntity> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity prt WHERE prt.expiryDate < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity prt WHERE prt.user = ?1")
    void deleteByUser(UserEntity user);
}
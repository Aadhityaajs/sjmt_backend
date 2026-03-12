package com.sjmt.SJMT.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.RefreshTokenEntity;
import com.sjmt.SJMT.Entity.UserEntity;

/**
 * Refresh Token Repository
 * @author SJMT Team
 * @version 1.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {
    
    Optional<RefreshTokenEntity> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.user = ?1")
    void deleteByUser(UserEntity user);
    
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiryDate < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.user = ?1")
    void revokeAllUserTokens(UserEntity user);
}
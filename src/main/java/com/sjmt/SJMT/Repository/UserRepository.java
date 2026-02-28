package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Entity.UserStatusEnum;

/**
 * User Repository
 * @author SJMT Team
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByEmail(String email);
    
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);

    List<UserEntity> findByStatus(UserStatusEnum status);
}
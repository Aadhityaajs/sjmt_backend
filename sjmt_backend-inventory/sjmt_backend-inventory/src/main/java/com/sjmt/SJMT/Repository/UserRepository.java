package com.sjmt.SJMT.Repository;

import java.util.List;
import java.util.Optional;

import com.sjmt.SJMT.Entity.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.UserEntity;

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



    // Add this method for attendance service
    List<UserEntity> findByStatus(UserStatusEnum status);

}
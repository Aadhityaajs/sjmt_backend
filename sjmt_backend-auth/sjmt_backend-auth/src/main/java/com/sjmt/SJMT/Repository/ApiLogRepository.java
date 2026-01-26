package com.sjmt.SJMT.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.ApiLog;

/**
 * API Log Repository
 * @author SJMT Team
 * @version 1.0
 */
@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    // Custom query methods (optional - for future use)
    List<ApiLog> findByUsername(String username);

    List<ApiLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<ApiLog> findByResponseCode(Integer responseCode);
}
package com.sjmt.SJMT.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sjmt.SJMT.Entity.AttendanceEntity;
import com.sjmt.SJMT.Entity.AttendanceStatusEnum;
import com.sjmt.SJMT.Entity.UserEntity;

/**
 * Attendance Repository
 * Data access layer for attendance operations
 *
 * @author SJMT Team
 * @version 1.0
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    /**
     * Find attendance by user and date
     */
    Optional<AttendanceEntity> findByUserAndAttendanceDate(UserEntity user, LocalDate attendanceDate);

    /**
     * Check if attendance exists for user on specific date
     */
    boolean existsByUserAndAttendanceDate(UserEntity user, LocalDate attendanceDate);

    /**
     * Get all attendance records for a specific date (Daily Report)
     */
    List<AttendanceEntity> findByAttendanceDateOrderByUserUsername(LocalDate attendanceDate);

    /**
     * Get attendance for a user within date range (Individual Report)
     */
    List<AttendanceEntity> findByUserAndAttendanceDateBetweenOrderByAttendanceDate(
            UserEntity user, LocalDate startDate, LocalDate endDate);

    /**
     * Get all attendance records for a date range
     */
    List<AttendanceEntity> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count attendance by status for a user in date range
     */
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.user = :user " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.status = :status")
    Long countByUserAndDateRangeAndStatus(
            @Param("user") UserEntity user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatusEnum status);

    /**
     * Get all attendance for multiple users on a specific date
     */
    @Query("SELECT a FROM AttendanceEntity a WHERE a.attendanceDate = :date " +
            "AND a.user.userId IN :userIds ORDER BY a.user.username")
    List<AttendanceEntity> findByDateAndUserIds(
            @Param("date") LocalDate date,
            @Param("userIds") List<Integer> userIds);

    /**
     * Find all attendance records created by a specific admin
     */
    List<AttendanceEntity> findByCreatedBy(UserEntity admin);

    /**
     * Get attendance count for a specific date
     */
    Long countByAttendanceDate(LocalDate attendanceDate);
}
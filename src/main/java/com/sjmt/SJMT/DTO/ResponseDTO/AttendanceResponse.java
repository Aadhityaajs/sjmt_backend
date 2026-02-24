package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sjmt.SJMT.Entity.AttendanceStatusEnum;

/**
 * Response DTO for attendance
 *
 * @author SJMT Team
 * @version 1.0
 */
public class AttendanceResponse {

    private Long attendanceId;
    private Integer userId;
    private String username;
    private String fullName;
    private LocalDate attendanceDate;
    private AttendanceStatusEnum status;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private String updatedByUsername;
    private LocalDateTime updatedAt;

    // Constructors
    public AttendanceResponse() {
    }

    // Getters and Setters
    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatusEnum status) {
        this.status = status;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AttendanceResponse{" +
                "attendanceId=" + attendanceId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", attendanceDate=" + attendanceDate +
                ", status=" + status +
                '}';
    }
}
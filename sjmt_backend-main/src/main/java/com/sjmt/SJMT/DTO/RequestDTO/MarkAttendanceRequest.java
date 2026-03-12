package com.sjmt.SJMT.DTO.RequestDTO;

import java.time.LocalDate;

import com.sjmt.SJMT.Entity.AttendanceStatusEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * Request DTO for marking attendance
 *
 * @author SJMT Team
 * @version 1.0
 */
public class MarkAttendanceRequest {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance date cannot be in the future")
    private LocalDate attendanceDate;

    @NotNull(message = "Status is required")
    private AttendanceStatusEnum status;

    // Constructors
    public MarkAttendanceRequest() {
    }

    public MarkAttendanceRequest(Integer userId, LocalDate attendanceDate, AttendanceStatusEnum status) {
        this.userId = userId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "MarkAttendanceRequest{" +
                "userId=" + userId +
                ", attendanceDate=" + attendanceDate +
                ", status=" + status +
                '}';
    }
}
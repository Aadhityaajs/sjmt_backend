package com.sjmt.SJMT.DTO.RequestDTO;

import com.sjmt.SJMT.Entity.AttendanceStatusEnum;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating attendance
 *
 * @author SJMT Team
 * @version 1.0
 */
public class UpdateAttendanceRequest {

    @NotNull(message = "Attendance ID is required")
    private Long attendanceId;

    @NotNull(message = "Status is required")
    private AttendanceStatusEnum status;

    // Constructors
    public UpdateAttendanceRequest() {
    }

    public UpdateAttendanceRequest(Long attendanceId, AttendanceStatusEnum status) {
        this.attendanceId = attendanceId;
        this.status = status;
    }

    // Getters and Setters
    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public AttendanceStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UpdateAttendanceRequest{" +
                "attendanceId=" + attendanceId +
                ", status=" + status +
                '}';
    }
}
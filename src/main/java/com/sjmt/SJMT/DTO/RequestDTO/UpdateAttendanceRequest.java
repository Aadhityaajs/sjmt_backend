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

    @NotNull(message = "Status is required")
    private AttendanceStatusEnum status;

    // Constructors
    public UpdateAttendanceRequest() {
    }

    public UpdateAttendanceRequest(AttendanceStatusEnum status) {
        this.status = status;
    }

    // Getters and Setters

    public AttendanceStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UpdateAttendanceRequest{" +
                "status=" + status +
                '}';
    }
}
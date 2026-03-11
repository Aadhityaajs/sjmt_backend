package com.sjmt.SJMT.DTO.ResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for daily attendance report
 *
 * @author SJMT Team
 * @version 1.0
 */
public class DailyReportResponse {

    private LocalDate attendanceDate;
    private Integer totalUsers;
    private Integer markedCount;
    private Integer unmarkedCount;
    private List<AttendanceResponse> attendanceList;

    // Constructors
    public DailyReportResponse() {
    }

    public DailyReportResponse(LocalDate attendanceDate, Integer totalUsers,
                               Integer markedCount, Integer unmarkedCount,
                               List<AttendanceResponse> attendanceList) {
        this.attendanceDate = attendanceDate;
        this.totalUsers = totalUsers;
        this.markedCount = markedCount;
        this.unmarkedCount = unmarkedCount;
        this.attendanceList = attendanceList;
    }

    // Getters and Setters
    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getMarkedCount() {
        return markedCount;
    }

    public void setMarkedCount(Integer markedCount) {
        this.markedCount = markedCount;
    }

    public Integer getUnmarkedCount() {
        return unmarkedCount;
    }

    public void setUnmarkedCount(Integer unmarkedCount) {
        this.unmarkedCount = unmarkedCount;
    }

    public List<AttendanceResponse> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<AttendanceResponse> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @Override
    public String toString() {
        return "DailyReportResponse{" +
                "attendanceDate=" + attendanceDate +
                ", totalUsers=" + totalUsers +
                ", markedCount=" + markedCount +
                ", unmarkedCount=" + unmarkedCount +
                '}';
    }
}
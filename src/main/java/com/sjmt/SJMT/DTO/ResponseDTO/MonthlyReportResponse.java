package com.sjmt.SJMT.DTO.ResponseDTO;

/**
 * Response DTO for monthly attendance report
 *
 * @author SJMT Team
 * @version 1.0
 */
public class MonthlyReportResponse {

    private Integer userId;
    private String username;
    private String fullName;
    private Integer month;
    private Integer year;
    private Integer totalWorkingDays;
    private Long presentDays;
    private Long absentDays;
    private Long halfDays;
    private Long companyHolidays;
    private Long generalHolidays;
    private Integer unmarkedDays;
    private Double attendancePercentage;

    // Constructors
    public MonthlyReportResponse() {
    }

    // Getters and Setters
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

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(Integer totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public Long getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(Long presentDays) {
        this.presentDays = presentDays;
    }

    public Long getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(Long absentDays) {
        this.absentDays = absentDays;
    }

    public Long getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(Long halfDays) {
        this.halfDays = halfDays;
    }

    public Long getCompanyHolidays() {
        return companyHolidays;
    }

    public void setCompanyHolidays(Long companyHolidays) {
        this.companyHolidays = companyHolidays;
    }

    public Long getGeneralHolidays() {
        return generalHolidays;
    }

    public void setGeneralHolidays(Long generalHolidays) {
        this.generalHolidays = generalHolidays;
    }

    public Integer getUnmarkedDays() {
        return unmarkedDays;
    }

    public void setUnmarkedDays(Integer unmarkedDays) {
        this.unmarkedDays = unmarkedDays;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    @Override
    public String toString() {
        return "MonthlyReportResponse{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", month=" + month +
                ", year=" + year +
                ", presentDays=" + presentDays +
                ", absentDays=" + absentDays +
                ", attendancePercentage=" + attendancePercentage +
                '}';
    }
}
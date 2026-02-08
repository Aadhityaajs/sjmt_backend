package com.sjmt.SJMT.DTO.RequestDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for monthly report
 *
 * @author SJMT Team
 * @version 1.0
 */
public class MonthlyReportRequest {

    private Integer userId; // Optional - if null, get report for all users

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer year;

    // Constructors
    public MonthlyReportRequest() {
    }

    public MonthlyReportRequest(Integer userId, Integer month, Integer year) {
        this.userId = userId;
        this.month = month;
        this.year = year;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "MonthlyReportRequest{" +
                "userId=" + userId +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
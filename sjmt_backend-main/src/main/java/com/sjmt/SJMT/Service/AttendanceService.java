package com.sjmt.SJMT.Service;

import java.time.LocalDate;
import java.util.List;

import com.sjmt.SJMT.DTO.ResponseDTO.AttendanceResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.DailyReportResponse;
import com.sjmt.SJMT.DTO.RequestDTO.MarkAttendanceRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.MonthlyReportResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.UnmarkedDatesResponse;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateAttendanceRequest;

/**
 * Attendance Service Interface
 * Defines business operations for attendance management
 *
 * @author SJMT Team
 * @version 1.0
 */
public interface AttendanceService {

    /**
     * Mark attendance for a user
     * @param request Mark attendance request
     * @param adminUsername Username of admin marking attendance
     * @return AttendanceResponse
     */
    AttendanceResponse markAttendance(MarkAttendanceRequest request, String adminUsername);

    /**
     * Update existing attendance record
     * @param id Attendance ID
     * @param request Update attendance request
     * @param adminUsername Username of admin updating attendance
     * @return AttendanceResponse
     */
    AttendanceResponse updateAttendance(Long id, UpdateAttendanceRequest request, String adminUsername);

    /**
     * Get daily attendance report for a specific date
     * @param date Attendance date
     * @return DailyReportResponse
     */
    DailyReportResponse getDailyReport(LocalDate date);

    /**
     * Get attendance records for a specific user within date range
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of AttendanceResponse
     */
    List<AttendanceResponse> getUserAttendance(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get monthly attendance report for a user
     * @param userId User ID
     * @param month Month (1-12)
     * @param year Year
     * @return MonthlyReportResponse
     */
    MonthlyReportResponse getMonthlyReport(Integer userId, Integer month, Integer year);

    /**
     * Get monthly reports for all users
     * @param month Month (1-12)
     * @param year Year
     * @return List of MonthlyReportResponse
     */
    List<MonthlyReportResponse> getAllUsersMonthlyReport(Integer month, Integer year);

    /**
     * Get unmarked dates within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of UnmarkedDatesResponse
     */
    List<UnmarkedDatesResponse> getUnmarkedDates(LocalDate startDate, LocalDate endDate);
}
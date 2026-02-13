package com.sjmt.SJMT.Controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sjmt.SJMT.DTO.RequestDTO.MarkAttendanceRequest;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateAttendanceRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.AttendanceResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.DailyReportResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.MonthlyReportResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.UnmarkedDatesResponse;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Repository.UserRepository;
import com.sjmt.SJMT.Service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Attendance Controller
 * REST API endpoints for attendance management
 *
 * @author SJMT Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/attendance")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Mark attendance for a user
     * POST /api/attendance/mark
     */
    @PostMapping("/mark")
    @Operation(summary = "Mark attendance", description = "Mark attendance for a user (Admin only)")
    public ResponseEntity<AttendanceResponse> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            Authentication authentication) {

        logger.info("Received request to mark attendance: {}", request);

        // Get current admin username from authentication
        String username = authentication.getName();

        // Fetch the UserEntity from database
        UserEntity admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + username));

        Integer adminUserId = admin.getUserId();

        AttendanceResponse response = attendanceService.markAttendance(request, adminUserId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update existing attendance record
     * PUT /api/attendance/update
     */
    @PutMapping("/update")
    @Operation(summary = "Update attendance", description = "Update existing attendance record (Admin only)")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @Valid @RequestBody UpdateAttendanceRequest request,
            Authentication authentication) {

        logger.info("Received request to update attendance: {}", request);

        // Get current admin username from authentication
        String username = authentication.getName();

        // Fetch the UserEntity from database
        UserEntity admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + username));

        Integer adminUserId = admin.getUserId();

        AttendanceResponse response = attendanceService.updateAttendance(request, adminUserId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get daily attendance report
     * GET /api/attendance/daily-report?date=2026-02-07
     */
    @GetMapping("/daily-report")
    @Operation(summary = "Get daily attendance report", description = "Get attendance report for all users on a specific date (Admin only)")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        logger.info("Received request for daily report: {}", date);

        DailyReportResponse response = attendanceService.getDailyReport(date);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get attendance for a specific user within date range
     * GET /api/attendance/user-report?userId=1&startDate=2026-02-01&endDate=2026-02-07
     */
    @GetMapping("/user-report")
    @Operation(summary = "Get user attendance report", description = "Get attendance records for a specific user within date range (Admin only)")
    public ResponseEntity<List<AttendanceResponse>> getUserReport(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Received request for user report: userId={}, startDate={}, endDate={}",
                userId, startDate, endDate);

        List<AttendanceResponse> response = attendanceService.getUserAttendance(userId, startDate, endDate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get monthly attendance report for a user
     * GET /api/attendance/monthly-report?userId=1&month=2&year=2026
     */
    @GetMapping("/monthly-report")
    @Operation(summary = "Get monthly attendance report", description = "Get monthly attendance report for a specific user (Admin only)")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam Integer userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        logger.info("Received request for monthly report: userId={}, month={}, year={}",
                userId, month, year);

        MonthlyReportResponse response = attendanceService.getMonthlyReport(userId, month, year);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get monthly reports for all users
     * GET /api/attendance/monthly-report-all?month=2&year=2026
     */
    @GetMapping("/monthly-report-all")
    @Operation(summary = "Get monthly reports for all users", description = "Get monthly attendance reports for all users (Admin only)")
    public ResponseEntity<List<MonthlyReportResponse>> getAllUsersMonthlyReport(
            @RequestParam Integer month,
            @RequestParam Integer year) {

        logger.info("Received request for all users monthly report: month={}, year={}", month, year);

        List<MonthlyReportResponse> response = attendanceService.getAllUsersMonthlyReport(month, year);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get unmarked dates within a date range
     * GET /api/attendance/unmarked-dates?startDate=2026-02-01&endDate=2026-02-07
     */
    @GetMapping("/unmarked-dates")
    @Operation(summary = "Get unmarked dates", description = "Get list of dates with incomplete attendance marking (Admin only)")
    public ResponseEntity<List<UnmarkedDatesResponse>> getUnmarkedDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Received request for unmarked dates: startDate={}, endDate={}", startDate, endDate);

        List<UnmarkedDatesResponse> response = attendanceService.getUnmarkedDates(startDate, endDate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
package com.sjmt.SJMT.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sjmt.SJMT.DTO.ResponseDTO.AttendanceResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.DailyReportResponse;
import com.sjmt.SJMT.DTO.RequestDTO.MarkAttendanceRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.MonthlyReportResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.UnmarkedDatesResponse;
import com.sjmt.SJMT.DTO.RequestDTO.UpdateAttendanceRequest;
import com.sjmt.SJMT.Entity.AttendanceEntity;
import com.sjmt.SJMT.Entity.AttendanceStatusEnum;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Entity.UserStatusEnum;
import com.sjmt.SJMT.Exception.AttendanceAlreadyExistsException;
import com.sjmt.SJMT.Exception.AttendanceNotFoundException;
import com.sjmt.SJMT.Exception.InvalidDateRangeException;
import com.sjmt.SJMT.Exception.UserNotActiveException;
import com.sjmt.SJMT.Exception.UserNotFoundException;
import com.sjmt.SJMT.Repository.AttendanceRepository;
import com.sjmt.SJMT.Repository.UserRepository;

/**
 * Attendance Service Implementation
 * Implements business logic for attendance management
 *
 * @author SJMT Team
 * @version 1.0
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);
    private static final int ALLOWED_PAST_DAYS = 14; // Admin can mark attendance for past 14 days

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AttendanceResponse markAttendance(MarkAttendanceRequest request, String adminUsername) {
        logger.info("Marking attendance for user: {}, date: {}, status: {}",
                request.getUserId(), request.getAttendanceDate(), request.getStatus());

        // Validate date range
        validateDateRange(request.getAttendanceDate());

        // Get user
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + request.getUserId()));

        // Check if user is active
        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new UserNotActiveException("Cannot mark attendance for inactive user: " + user.getUsername());
        }

        // Check for duplicate attendance
        if (attendanceRepository.existsByUserAndAttendanceDate(user, request.getAttendanceDate())) {
            throw new AttendanceAlreadyExistsException(
                    "Attendance already exists for user " + user.getUsername() +
                            " on date " + request.getAttendanceDate());
        }

        // Get admin user
        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found with username: " + adminUsername));

        // Create attendance record
        AttendanceEntity attendance = new AttendanceEntity(
                user,
                request.getAttendanceDate(),
                request.getStatus(),
                admin
        );

        AttendanceEntity savedAttendance = attendanceRepository.save(attendance);

        logger.info("Attendance marked successfully with ID: {}", savedAttendance.getAttendanceId());

        return mapToAttendanceResponse(savedAttendance);
    }

    @Override
    public AttendanceResponse updateAttendance(Long id, UpdateAttendanceRequest request, String adminUsername) {
        logger.info("Updating attendance ID: {}, new status: {}",
                id, request.getStatus());

        // Get attendance record
        AttendanceEntity attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AttendanceNotFoundException(
                        "Attendance record not found with ID: " + id));

        // Validate that date is still within modifiable range
        validateDateRange(attendance.getAttendanceDate());

        // Get admin user
        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found with username: " + adminUsername));

        // Update attendance
        attendance.setStatus(request.getStatus());
        attendance.setUpdatedBy(admin);

        AttendanceEntity updatedAttendance = attendanceRepository.save(attendance);

        logger.info("Attendance updated successfully: {}", updatedAttendance.getAttendanceId());

        return mapToAttendanceResponse(updatedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyReportResponse getDailyReport(LocalDate date) {
        logger.info("Generating daily report for date: {}", date);

        // Get all active users
        List<UserEntity> activeUsers = userRepository.findByStatus(UserStatusEnum.ACTIVE);

        // Get all attendance for the date
        List<AttendanceEntity> attendanceList = attendanceRepository.findByAttendanceDateOrderByUserUsername(date);

        // Convert to response DTOs
        List<AttendanceResponse> attendanceResponses = attendanceList.stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());

        // Calculate statistics
        int totalUsers = activeUsers.size();
        int markedCount = attendanceList.size();
        int unmarkedCount = totalUsers - markedCount;

        DailyReportResponse response = new DailyReportResponse(
                date,
                totalUsers,
                markedCount,
                unmarkedCount,
                attendanceResponses
        );

        logger.info("Daily report generated: Total={}, Marked={}, Unmarked={}",
                totalUsers, markedCount, unmarkedCount);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getUserAttendance(Integer userId, LocalDate startDate, LocalDate endDate) {
        logger.info("Getting attendance for user: {}, from: {} to: {}", userId, startDate, endDate);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date cannot be after end date");
        }

        // Get user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Get attendance records
        List<AttendanceEntity> attendanceList = attendanceRepository
                .findByUserAndAttendanceDateBetweenOrderByAttendanceDate(user, startDate, endDate);

        return attendanceList.stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Integer userId, Integer month, Integer year) {
        logger.info("Generating monthly report for user: {}, month: {}, year: {}", userId, month, year);

        // Get user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new UserNotActiveException("Cannot generate monthly report for inactive user: " + user.getUsername());
        }

        // Calculate date range for the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all attendance for the month
        List<AttendanceEntity> attendanceList = attendanceRepository
                .findByUserAndAttendanceDateBetweenOrderByAttendanceDate(user, startDate, endDate);

        // Calculate statistics
        long presentDays = attendanceRepository.countByUserAndDateRangeAndStatus(
                user, startDate, endDate, AttendanceStatusEnum.PRESENT);

        long absentDays = attendanceRepository.countByUserAndDateRangeAndStatus(
                user, startDate, endDate, AttendanceStatusEnum.ABSENT);

        long halfDays = attendanceRepository.countByUserAndDateRangeAndStatus(
                user, startDate, endDate, AttendanceStatusEnum.HALF_DAY);

        long companyHolidays = attendanceRepository.countByUserAndDateRangeAndStatus(
                user, startDate, endDate, AttendanceStatusEnum.COMPANY_HOLIDAY);

        long generalHolidays = attendanceRepository.countByUserAndDateRangeAndStatus(
                user, startDate, endDate, AttendanceStatusEnum.GENERAL_HOLIDAY);

        int totalWorkingDays = yearMonth.lengthOfMonth();
        int markedDays = attendanceList.size();
        int unmarkedDays = totalWorkingDays - markedDays;

        // Calculate attendance percentage (considering only working days, excluding holidays)
        long totalHolidays = companyHolidays + generalHolidays;
        long actualWorkingDays = totalWorkingDays - totalHolidays - unmarkedDays;
        double attendancePercentage = 0.0;

        if (actualWorkingDays > 0) {
            attendancePercentage = ((double) (presentDays + (halfDays * 0.5)) / actualWorkingDays) * 100;
            attendancePercentage = Math.round(attendancePercentage * 100.0) / 100.0; // Round to 2 decimal places
        }

        // Build response
        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setMonth(month);
        response.setYear(year);
        response.setTotalWorkingDays(totalWorkingDays);
        response.setPresentDays(presentDays);
        response.setAbsentDays(absentDays);
        response.setHalfDays(halfDays);
        response.setCompanyHolidays(companyHolidays);
        response.setGeneralHolidays(generalHolidays);
        response.setUnmarkedDays(unmarkedDays);
        response.setAttendancePercentage(attendancePercentage);

        logger.info("Monthly report generated for user: {}, attendance: {}%", userId, attendancePercentage);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyReportResponse> getAllUsersMonthlyReport(Integer month, Integer year) {
        logger.info("Generating monthly report for all users, month: {}, year: {}", month, year);

        List<UserEntity> activeUsers = userRepository.findByStatus(UserStatusEnum.ACTIVE);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttendanceEntity> allAttendance = attendanceRepository
                .findByAttendanceDateBetween(startDate, endDate);

        Map<Integer, List<AttendanceEntity>> attendanceByUser = allAttendance.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getUserId()));

        return activeUsers.stream().map(user -> {
            List<AttendanceEntity> userAttendance = attendanceByUser.getOrDefault(user.getUserId(), new ArrayList<>());
            
            long presentDays = userAttendance.stream().filter(a -> a.getStatus() == AttendanceStatusEnum.PRESENT).count();
            long absentDays = userAttendance.stream().filter(a -> a.getStatus() == AttendanceStatusEnum.ABSENT).count();
            long halfDays = userAttendance.stream().filter(a -> a.getStatus() == AttendanceStatusEnum.HALF_DAY).count();
            long companyHolidays = userAttendance.stream().filter(a -> a.getStatus() == AttendanceStatusEnum.COMPANY_HOLIDAY).count();
            long generalHolidays = userAttendance.stream().filter(a -> a.getStatus() == AttendanceStatusEnum.GENERAL_HOLIDAY).count();
            
            int totalWorkingDays = yearMonth.lengthOfMonth();
            int markedDays = userAttendance.size();
            int unmarkedDays = totalWorkingDays - markedDays;

            long totalHolidays = companyHolidays + generalHolidays;
            long actualWorkingDays = totalWorkingDays - totalHolidays - unmarkedDays;
            double attendancePercentage = 0.0;

            if (actualWorkingDays > 0) {
                attendancePercentage = ((double) (presentDays + (halfDays * 0.5)) / actualWorkingDays) * 100;
                attendancePercentage = Math.round(attendancePercentage * 100.0) / 100.0;
            }

            MonthlyReportResponse response = new MonthlyReportResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
            response.setMonth(month);
            response.setYear(year);
            response.setTotalWorkingDays(totalWorkingDays);
            response.setPresentDays(presentDays);
            response.setAbsentDays(absentDays);
            response.setHalfDays(halfDays);
            response.setCompanyHolidays(companyHolidays);
            response.setGeneralHolidays(generalHolidays);
            response.setUnmarkedDays(unmarkedDays);
            response.setAttendancePercentage(attendancePercentage);

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnmarkedDatesResponse> getUnmarkedDates(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting unmarked dates from: {} to: {}", startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date cannot be after end date");
        }

        List<UserEntity> activeUsers = userRepository.findByStatus(UserStatusEnum.ACTIVE);
        int totalActiveUsers = activeUsers.size();

        List<AttendanceEntity> allAttendanceInRange = attendanceRepository
                .findByAttendanceDateBetween(startDate, endDate);

        Map<LocalDate, List<AttendanceEntity>> attendanceByDate = allAttendanceInRange.stream()
                .collect(Collectors.groupingBy(AttendanceEntity::getAttendanceDate));

        List<UnmarkedDatesResponse> unmarkedDatesList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<AttendanceEntity> markedAttendance = attendanceByDate.getOrDefault(currentDate, new ArrayList<>());
            long markedCount = markedAttendance.size();
            int unmarkedCount = (int) (totalActiveUsers - markedCount);

            if (unmarkedCount > 0) {
                List<Integer> markedUserIds = markedAttendance.stream()
                        .map(a -> a.getUser().getUserId())
                        .collect(Collectors.toList());

                List<UnmarkedDatesResponse.UserBasicInfo> unmarkedUsers = activeUsers.stream()
                        .filter(user -> !markedUserIds.contains(user.getUserId()))
                        .map(user -> new UnmarkedDatesResponse.UserBasicInfo(
                                user.getUserId(),
                                user.getUsername(),
                                user.getFullName()))
                        .collect(Collectors.toList());

                UnmarkedDatesResponse response = new UnmarkedDatesResponse();
                response.setDate(currentDate);
                response.setTotalActiveUsers(totalActiveUsers);
                response.setMarkedCount((int) markedCount);
                response.setUnmarkedCount(unmarkedCount);
                response.setUnmarkedUsersList(unmarkedUsers);

                unmarkedDatesList.add(response);
            }

            currentDate = currentDate.plusDays(1);
        }

        logger.info("Found {} dates with unmarked attendance", unmarkedDatesList.size());

        return unmarkedDatesList;
    }

    /**
     * Validate that date is within allowed range (today to past 14 days)
     */
    private void validateDateRange(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate earliestAllowedDate = today.minusDays(ALLOWED_PAST_DAYS);

        if (date.isAfter(today)) {
            throw new InvalidDateRangeException("Cannot mark attendance for future dates");
        }

        if (date.isBefore(earliestAllowedDate)) {
            throw new InvalidDateRangeException(
                    "Can only mark attendance for current day and past " + ALLOWED_PAST_DAYS + " days. " +
                            "Date " + date + " is too old.");
        }
    }

    /**
     * Map AttendanceEntity to AttendanceResponse DTO
     */
    private AttendanceResponse mapToAttendanceResponse(AttendanceEntity attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(attendance.getAttendanceId());
        response.setUserId(attendance.getUser().getUserId());
        response.setUsername(attendance.getUser().getUsername());
        response.setFullName(attendance.getUser().getFullName());
        response.setAttendanceDate(attendance.getAttendanceDate());
        response.setStatus(attendance.getStatus());
        response.setCreatedByUsername(attendance.getCreatedBy().getUsername());
        response.setCreatedAt(attendance.getCreatedAt());

        if (attendance.getUpdatedBy() != null) {
            response.setUpdatedByUsername(attendance.getUpdatedBy().getUsername());
        }
        response.setUpdatedAt(attendance.getUpdatedAt());

        return response;
    }
}
package com.sjmt.SJMT.Exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.Service.ErrorNotificationService;

/**
 * Global Exception Handler
 * @author SJMT Team
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Optional — only present when app.error-notification.enabled=true.
     * Spring injects null if the bean is not active (safe to null-check).
     */
    @Autowired(required = false)
    private ErrorNotificationService errorNotificationService;
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.error("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
    }
    
    /**
     * Handle bad credentials (wrong username/password)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        logger.error("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid username or password"));
    }
    
    /**
     * Handle user not found
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(
            UsernameNotFoundException ex, WebRequest request) {
        logger.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * Handle access denied (insufficient permissions)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        logger.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access denied. You don't have permission to access this resource."));
    }
    
    /**
     * Handle AttendanceAlreadyExistsException
     * HTTP Status: 409 Conflict
     */
    @ExceptionHandler(AttendanceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttendanceAlreadyExists(
            AttendanceAlreadyExistsException ex, WebRequest request) {
        logger.warn("Attendance already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle AttendanceNotFoundException
     * HTTP Status: 404 Not Found
     */
    @ExceptionHandler(AttendanceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttendanceNotFound(
            AttendanceNotFoundException ex, WebRequest request) {
        logger.warn("Attendance not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle UserNotFoundException
     * HTTP Status: 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
            UserNotFoundException ex, WebRequest request) {
        logger.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle UserNotActiveException
     * HTTP Status: 400 Bad Request
     */
    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotActive(
            UserNotActiveException ex, WebRequest request) {
        logger.warn("User not active: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle InvalidDateRangeException
     * HTTP Status: 400 Bad Request
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDateRange(
            InvalidDateRangeException ex, WebRequest request) {
        logger.warn("Invalid date range: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle ResourceNotFoundException
     * HTTP Status: 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle DuplicateResourceException
     * HTTP Status: 409 Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        logger.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * MEDIUM-8: Handle optimistic locking conflicts — another user saved the same
     * record between the time the current user loaded it and tried to save.
     * Returns 409 Conflict with a human-readable message so the UI can prompt a reload.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex, WebRequest request) {
        logger.warn("Optimistic locking conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "This record was modified by another user while you were editing. " +
                        "Please refresh the page and try again."));
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception: {}", ex.getMessage());
        if (errorNotificationService != null) {
            errorNotificationService.sendErrorAlert(
                "Backend – RuntimeException",
                ex.getMessage(),
                stackTraceOf(ex),
                request.getDescription(false)
            );
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        if (errorNotificationService != null) {
            errorNotificationService.sendErrorAlert(
                "Backend – Unexpected Exception",
                ex.getMessage(),
                stackTraceOf(ex),
                request.getDescription(false)
            );
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String stackTraceOf(Throwable t) {
        if (t == null) return null;
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
package com.sjmt.SJMT.Exception;

/**
 * Exception thrown when attendance record is not found
 *
 * @author SJMT Team
 * @version 1.0
 */
public class AttendanceNotFoundException extends RuntimeException {

    public AttendanceNotFoundException(String message) {
        super(message);
    }

    public AttendanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.sjmt.SJMT.Exception;

/**
 * Exception thrown when attendance already exists for user on a specific date
 *
 * @author SJMT Team
 * @version 1.0
 */
public class AttendanceAlreadyExistsException extends RuntimeException {

    public AttendanceAlreadyExistsException(String message) {
        super(message);
    }

    public AttendanceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
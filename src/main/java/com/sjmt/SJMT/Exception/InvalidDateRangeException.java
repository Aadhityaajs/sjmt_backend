package com.sjmt.SJMT.Exception;

/**
 * Exception thrown when date is outside allowed range
 *
 * @author SJMT Team
 * @version 1.0
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }

    public InvalidDateRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
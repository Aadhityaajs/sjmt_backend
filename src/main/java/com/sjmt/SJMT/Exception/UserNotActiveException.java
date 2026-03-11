package com.sjmt.SJMT.Exception;

/**
 * Exception thrown when trying to mark attendance for inactive user
 *
 * @author SJMT Team
 * @version 1.0
 */
public class UserNotActiveException extends RuntimeException {

    public UserNotActiveException(String message) {
        super(message);
    }

    public UserNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
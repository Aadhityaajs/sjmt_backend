package com.sjmt.SJMT.Exception;

/**
 * Exception thrown when user is not found
 *
 * @author SJMT Team
 * @version 1.0
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
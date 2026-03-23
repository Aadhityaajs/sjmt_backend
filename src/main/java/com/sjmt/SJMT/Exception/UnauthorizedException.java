package com.sjmt.SJMT.Exception;

/**
 * Exception thrown when user is not authorized to perform action
 *
 * @author SJMT Team
 * @version 1.0
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
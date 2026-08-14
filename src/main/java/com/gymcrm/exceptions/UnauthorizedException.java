package com.gymcrm.exceptions;

/**
 * Thrown when credentials are missing or invalid for a protected REST call.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}

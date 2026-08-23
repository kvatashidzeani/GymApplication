package com.gymcrm.exceptions;

/**
 * Thrown when a username is temporarily blocked after too many failed logins.
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }
}

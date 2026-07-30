package com.gymcrm.validators;

import java.time.LocalDate;

/**
 * Shared required-field checks for REST endpoints.
 */
public final class RequestValidation {

    private RequestValidation() {
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    public static String requirePassword(String password) {
        return requireNonBlank(password, "Password");
    }

    public static String requireUsername(String username) {
        return requireNonBlank(username, "Username");
    }

    public static void requireSameUsername(String pathUsername, String bodyUsername) {
        String path = requireUsername(pathUsername);
        String body = requireNonBlank(bodyUsername, "Username");
        if (!path.equals(body)) {
            throw new IllegalArgumentException("Username in path and body must match");
        }
    }

    public static void requirePeriodOrder(LocalDate periodFrom, LocalDate periodTo) {
        if (periodFrom != null && periodTo != null && periodFrom.isAfter(periodTo)) {
            throw new IllegalArgumentException("Period From cannot be after Period To");
        }
    }
}

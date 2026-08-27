package com.gymcrm.security;

/**
 * Holds the inbound request JWT for the current thread so microservice clients
 * can forward {@code Authorization: Bearer ...} to secondary services.
 */
public final class JwtTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private JwtTokenHolder() {
    }

    public static void set(String token) {
        TOKEN.set(token);
    }

    public static String get() {
        return TOKEN.get();
    }

    public static void clear() {
        TOKEN.remove();
    }
}

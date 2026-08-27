package com.gymcrm.security;

import org.springframework.stereotype.Component;

/**
 * Supplies a cached service JWT for Gym CRM → trainer-workload calls when no
 * end-user Bearer token is available on the current request.
 */
@Component
public class ServiceJwtProvider {

    private final JwtService jwtService;
    private final Object lock = new Object();

    private volatile String cachedToken;
    private volatile long cachedExpiresAtEpochMs;

    public ServiceJwtProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String getToken() {
        long now = System.currentTimeMillis();
        String token = cachedToken;
        // Refresh 60s before expiry
        if (token != null && now < cachedExpiresAtEpochMs - 60_000L) {
            return token;
        }
        synchronized (lock) {
            now = System.currentTimeMillis();
            if (cachedToken != null && now < cachedExpiresAtEpochMs - 60_000L) {
                return cachedToken;
            }
            cachedToken = jwtService.generateServiceToken();
            cachedExpiresAtEpochMs = jwtService.extractExpiration(cachedToken).getTime();
            return cachedToken;
        }
    }
}

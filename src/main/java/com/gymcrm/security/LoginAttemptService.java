package com.gymcrm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Brute-force protector: after {@value #MAX_FAILED_ATTEMPTS} failed logins,
 * the username is blocked for {@value #LOCK_DURATION_MINUTES} minutes.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_DURATION_MINUTES = 5;

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(LOCK_DURATION_MINUTES);

    private final ConcurrentMap<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        AttemptState state = attempts.get(normalize(username));
        if (state == null || state.lockedUntil == null) {
            return false;
        }
        Instant now = clock.instant();
        if (now.isBefore(state.lockedUntil)) {
            return true;
        }
        // Lock expired — clear so the user can try again
        attempts.remove(normalize(username), state);
        return false;
    }

    public void loginSucceeded(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        attempts.remove(normalize(username));
        log.debug("Login attempts reset for username={}", username);
    }

    public void loginFailed(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = normalize(username);
        attempts.compute(key, (k, existing) -> {
            Instant now = clock.instant();
            if (existing != null && existing.lockedUntil != null && now.isBefore(existing.lockedUntil)) {
                return existing;
            }
            int failures = (existing == null || existing.lockedUntil != null) ? 1 : existing.failedAttempts + 1;
            Instant lockedUntil = null;
            if (failures >= MAX_FAILED_ATTEMPTS) {
                lockedUntil = now.plus(LOCK_DURATION);
                log.warn("Username={} blocked for {} minutes after {} failed logins",
                        username, LOCK_DURATION_MINUTES, failures);
            } else {
                log.info("Failed login attempt {}/{} for username={}",
                        failures, MAX_FAILED_ATTEMPTS, username);
            }
            return new AttemptState(failures, lockedUntil);
        });
    }

    private static String normalize(String username) {
        return username.trim();
    }

    private static final class AttemptState {
        private final int failedAttempts;
        private final Instant lockedUntil;

        private AttemptState(int failedAttempts, Instant lockedUntil) {
            this.failedAttempts = failedAttempts;
            this.lockedUntil = lockedUntil;
        }
    }
}

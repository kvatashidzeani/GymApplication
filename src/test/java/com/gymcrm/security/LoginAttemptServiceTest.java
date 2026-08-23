package com.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-23T12:00:00Z"));
        service = new LoginAttemptService(clock);
    }

    @Test
    void notBlocked_initially() {
        assertFalse(service.isBlocked("Ani.Smith"));
    }

    @Test
    void blocksAfterThreeFailures() {
        service.loginFailed("Ani.Smith");
        service.loginFailed("Ani.Smith");
        assertFalse(service.isBlocked("Ani.Smith"));

        service.loginFailed("Ani.Smith");
        assertTrue(service.isBlocked("Ani.Smith"));
    }

    @Test
    void successResetsFailures() {
        service.loginFailed("Ani.Smith");
        service.loginFailed("Ani.Smith");
        service.loginSucceeded("Ani.Smith");

        service.loginFailed("Ani.Smith");
        assertFalse(service.isBlocked("Ani.Smith"));
    }

    @Test
    void unlocksAfterFiveMinutes() {
        service.loginFailed("Ani.Smith");
        service.loginFailed("Ani.Smith");
        service.loginFailed("Ani.Smith");
        assertTrue(service.isBlocked("Ani.Smith"));

        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        assertFalse(service.isBlocked("Ani.Smith"));
    }

    @Test
    void lockIsPerUsername() {
        service.loginFailed("Ani.Smith");
        service.loginFailed("Ani.Smith");
        service.loginFailed("Ani.Smith");

        assertTrue(service.isBlocked("Ani.Smith"));
        assertFalse(service.isBlocked("Mike.Brown"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}

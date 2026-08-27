package com.gymcrm.workload.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadJwtServiceTest {

    private static final String SECRET = "GymCrmJwtSecretKey-ChangeMe-AtLeast32CharsLong!";

    private WorkloadJwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new WorkloadJwtService(SECRET);
    }

    @Test
    void rejectsGarbageToken() {
        assertFalse(jwtService.isTokenValid("not-a-jwt"));
    }

    @Test
    void acceptsTokenSignedWithSharedSecret() {
        // Build with same algorithm/secret as Gym CRM JwtService
        String token = io.jsonwebtoken.Jwts.builder()
                .subject("gym-crm")
                .claim("typ", "service")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("gym-crm", jwtService.extractUsername(token));
    }
}

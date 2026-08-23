package com.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "GymCrmJwtSecretKey-ChangeMe-AtLeast32CharsLong!",
                3_600_000L);
    }

    @Test
    void generateAndValidateToken() {
        UserDetails user = User.withUsername("Ani.Smith").password("x").roles("TRAINEE").build();
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("Ani.Smith", jwtService.extractUsername(token));
        assertNotNull(jwtService.extractJti(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_rejectsWrongUser() {
        UserDetails owner = User.withUsername("Ani.Smith").password("x").roles("TRAINEE").build();
        UserDetails other = User.withUsername("Mike.Brown").password("x").roles("TRAINER").build();
        String token = jwtService.generateToken(owner);

        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void isTokenValid_rejectsTamperedToken() {
        UserDetails user = User.withUsername("Ani.Smith").password("x").roles("TRAINEE").build();
        String token = jwtService.generateToken(user) + "x";

        assertFalse(jwtService.isTokenValid(token, user));
    }
}

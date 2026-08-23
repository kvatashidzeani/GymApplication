package com.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Spring Security login: AuthenticationManager + BCrypt + UserDetails.
 */
class LoginAuthenticationTest {

    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        UserDetailsService userDetailsService = username -> User.builder()
                .username("Ani.Smith")
                .password(encoder.encode("secret12"))
                .roles("TRAINEE")
                .build();

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        authenticationManager = new ProviderManager(provider);
    }

    @Test
    void authenticate_validPassword_succeeds() {
        Authentication result = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("Ani.Smith", "secret12"));

        assertTrue(result.isAuthenticated());
        assertEquals("Ani.Smith", result.getName());
    }

    @Test
    void authenticate_wrongPassword_fails() {
        assertThrows(BadCredentialsException.class, () ->
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("Ani.Smith", "wrong")));
    }
}

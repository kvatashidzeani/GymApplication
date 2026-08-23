package com.gymcrm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

/**
 * On logout, blacklists the presented JWT so it cannot be reused.
 */
@Component
public class JwtLogoutHandler implements LogoutHandler {

    private static final Logger log = LoggerFactory.getLogger(JwtLogoutHandler.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final JwtTokenBlacklist tokenBlacklist;

    public JwtLogoutHandler(JwtService jwtService, JwtTokenBlacklist tokenBlacklist) {
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return;
        }
        try {
            String jti = jwtService.extractJti(token);
            Date expiration = jwtService.extractExpiration(token);
            Instant until = expiration != null ? expiration.toInstant() : Instant.now().plusSeconds(3600);
            tokenBlacklist.blacklist(jti, until);
            log.info("JWT blacklisted on logout jti={}", jti);
        } catch (RuntimeException ex) {
            log.warn("Could not blacklist JWT on logout: {}", ex.getMessage());
        }
    }
}

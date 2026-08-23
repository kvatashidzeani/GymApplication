package com.gymcrm.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Sets a fake authenticated principal on {@link SecurityContextHolder} for unit tests
 * that invoke controllers directly (no Spring Security filter chain).
 */
public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static void authenticate(String username) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "n/a",
                        AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}

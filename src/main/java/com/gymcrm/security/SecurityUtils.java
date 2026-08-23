package com.gymcrm.security;

import com.gymcrm.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helpers for reading the Spring Security authenticated principal in controllers.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Unauthorized");
        }
        return authentication.getName();
    }

    /**
     * Ensures the authenticated principal matches the resource username from the path/body.
     */
    public static String requireSelf(String resourceUsername) {
        String authenticated = currentUsername();
        if (resourceUsername == null || !authenticated.equals(resourceUsername.trim())) {
            throw new UnauthorizedException("Unauthorized");
        }
        return authenticated;
    }
}

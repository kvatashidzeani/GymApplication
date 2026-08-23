package com.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    void corsConfigurationSource_allowsLocalhostAndAuthorizationHeader() {
        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(
                "http://localhost:*,http://127.0.0.1:*",
                "GET,POST,PUT,PATCH,DELETE,OPTIONS",
                "*",
                "Authorization",
                true,
                3600L);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/trainees/register");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(config.getAllowedOriginPatterns().contains("http://localhost:*"));
        assertTrue(config.getAllowedMethods().contains(HttpMethod.GET.name()));
        assertTrue(config.getAllowedMethods().contains(HttpMethod.OPTIONS.name()));
        assertTrue(config.getExposedHeaders().contains("Authorization"));
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
        assertEquals(3600L, config.getMaxAge());
    }
}

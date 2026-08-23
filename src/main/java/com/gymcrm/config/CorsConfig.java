package com.gymcrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS policy used by Spring Security ({@code http.cors()}).
 * Origins/methods are configurable via {@code gymcrm.cors.*} properties.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${gymcrm.cors.allowed-origin-patterns}") String allowedOriginPatterns,
            @Value("${gymcrm.cors.allowed-methods}") String allowedMethods,
            @Value("${gymcrm.cors.allowed-headers}") String allowedHeaders,
            @Value("${gymcrm.cors.exposed-headers}") String exposedHeaders,
            @Value("${gymcrm.cors.allow-credentials}") boolean allowCredentials,
            @Value("${gymcrm.cors.max-age}") long maxAge) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(split(allowedOriginPatterns));
        config.setAllowedMethods(split(allowedMethods));
        config.setAllowedHeaders(split(allowedHeaders));
        config.setExposedHeaders(split(exposedHeaders));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static List<String> split(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

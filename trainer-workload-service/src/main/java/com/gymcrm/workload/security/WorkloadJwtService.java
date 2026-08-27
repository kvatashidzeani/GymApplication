package com.gymcrm.workload.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Validates HS256 JWTs issued by Gym CRM (shared secret).
 */
@Service
public class WorkloadJwtService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadJwtService.class);

    private final SecretKey secretKey;

    public WorkloadJwtService(@Value("${gymcrm.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(resolveKeyBytes(secret));
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException
                 | SecurityException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] resolveKeyBytes(String secret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (RuntimeException ignored) {
            // plain UTF-8
        }
        byte[] utf8 = secret.getBytes(StandardCharsets.UTF_8);
        if (utf8.length < 32) {
            throw new IllegalStateException(
                    "gymcrm.jwt.secret must be at least 32 bytes (256 bits) for HS256");
        }
        return utf8;
    }
}

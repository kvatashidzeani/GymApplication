package com.gymcrm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory blacklist of JWT ids (jti) used after logout until natural expiry.
 */
@Service
public class JwtTokenBlacklist {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenBlacklist.class);

    private final ConcurrentHashMap<String, Instant> blacklistedUntil = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        Instant until = expiresAt != null ? expiresAt : Instant.now().plusSeconds(3600);
        blacklistedUntil.put(jti, until);
        log.debug("JWT jti={} blacklisted until {}", jti, until);
        purgeExpired();
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Instant until = blacklistedUntil.get(jti);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            blacklistedUntil.remove(jti, until);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> it = blacklistedUntil.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Instant> entry = it.next();
            if (now.isAfter(entry.getValue())) {
                it.remove();
            }
        }
    }
}

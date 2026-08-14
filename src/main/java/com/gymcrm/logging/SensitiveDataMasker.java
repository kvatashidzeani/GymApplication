package com.gymcrm.logging;

import java.util.regex.Pattern;

/**
 * Masks sensitive fields before writing REST request details to logs.
 */
final class SensitiveDataMasker {

    private static final Pattern QUERY_PASSWORD = Pattern.compile(
            "(?i)(password|oldPassword|newPassword)=([^&]*)");
    private static final Pattern JSON_PASSWORD = Pattern.compile(
            "(?i)(\"(?:password|oldPassword|newPassword)\"\\s*:\\s*)\"[^\"]*\"");

    private SensitiveDataMasker() {
    }

    static String maskQuery(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }
        return QUERY_PASSWORD.matcher(query).replaceAll("$1=***");
    }

    static String maskBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        return JSON_PASSWORD.matcher(body).replaceAll("$1\"***\"");
    }
}

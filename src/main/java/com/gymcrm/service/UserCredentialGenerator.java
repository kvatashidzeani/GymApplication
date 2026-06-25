package com.gymcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UserCredentialGenerator {

    private static final Logger logger = LoggerFactory.getLogger(UserCredentialGenerator.class);

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a username from first and last name using dot notation.
     * If duplicate count > 0, appends a serial number suffix.
     *
     * @param firstName    the first name
     * @param lastName     the last name
     * @param duplicateCount number of existing users with the same name
     * @return generated unique username
     */
    public String generateUsername(String firstName, String lastName, long duplicateCount) {
        String baseUsername = firstName + "." + lastName;
        String username = duplicateCount == 0 ? baseUsername : baseUsername + duplicateCount;
        logger.debug("Generated username: {} (duplicateCount={})", username, duplicateCount);
        return username;
    }

    /**
     * Generates a random 10-character alphanumeric password.
     *
     * @return generated password
     */
    public String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        logger.debug("Generated random password for new user");
        return sb.toString();
    }
}
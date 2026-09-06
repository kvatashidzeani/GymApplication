package com.gymcrm.component;

import org.springframework.stereotype.Component;

/**
 * Shares HTTP response state between Cucumber step definitions.
 */
@Component
public class ScenarioContext {

    private int lastStatus;
    private String lastBody;
    private String registeredUsername;
    private String registeredPassword;
    private String bearerToken;

    public int getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(int lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLastBody() {
        return lastBody;
    }

    public void setLastBody(String lastBody) {
        this.lastBody = lastBody;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }

    public void setRegisteredUsername(String registeredUsername) {
        this.registeredUsername = registeredUsername;
    }

    public String getRegisteredPassword() {
        return registeredPassword;
    }

    public void setRegisteredPassword(String registeredPassword) {
        this.registeredPassword = registeredPassword;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public void reset() {
        lastStatus = 0;
        lastBody = null;
        registeredUsername = null;
        registeredPassword = null;
        bearerToken = null;
    }
}

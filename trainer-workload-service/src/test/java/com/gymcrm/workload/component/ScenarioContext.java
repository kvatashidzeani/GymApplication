package com.gymcrm.workload.component;

import org.springframework.stereotype.Component;

/**
 * Shares HTTP response and auth state between Cucumber step definitions.
 */
@Component
public class ScenarioContext {

    private int lastStatus;
    private String lastBody;
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

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public void reset() {
        lastStatus = 0;
        lastBody = null;
        bearerToken = null;
    }
}

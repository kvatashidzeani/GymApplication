package com.gymcrm.integration;

import org.springframework.stereotype.Component;

/**
 * Shares state between cross-microservice Cucumber steps.
 */
@Component("integrationScenarioContext")
public class IntegrationScenarioContext {

    private int lastStatus;
    private String lastBody;
    private String traineeUsername;
    private String traineePassword;
    private String trainerUsername;
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

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTraineePassword() {
        return traineePassword;
    }

    public void setTraineePassword(String traineePassword) {
        this.traineePassword = traineePassword;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
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
        traineeUsername = null;
        traineePassword = null;
        trainerUsername = null;
        bearerToken = null;
    }
}

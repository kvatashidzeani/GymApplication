package com.gymcrm.dto;

/**
 * Request body for Activate / De-Activate Trainee or Trainer (PATCH).
 */
public class ActivateRequest {

    private String username;
    private Boolean isActive;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}

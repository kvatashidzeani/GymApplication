package com.gymcrm.dto;

/**
 * Response body for Trainee/Trainer Registration.
 * Includes generated credentials and a JWT for immediate API access.
 */
public class RegistrationResponse {

    private String username;
    private String password;
    private String token;
    private String type = "Bearer";

    public RegistrationResponse() {
    }

    public RegistrationResponse(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

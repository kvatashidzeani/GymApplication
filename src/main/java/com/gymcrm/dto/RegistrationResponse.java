package com.gymcrm.dto;

public class RegistrationResponse {
    private String username;
    private String password;

    public RegistrationResponse () {
    }
    public RegistrationResponse(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public void setUserName(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setUsername(String password) {
        this.username = username;
    }
}

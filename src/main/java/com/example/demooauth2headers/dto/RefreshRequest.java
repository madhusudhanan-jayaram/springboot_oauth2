package com.example.demooauth2headers.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Default constructor
    public RefreshRequest() {}

    // Constructor
    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
package com.example.demooauth2headers.dto;

public class AuthResponse {
    
    private String message;

    // Default constructor
    public AuthResponse() {}

    // Constructor
    public AuthResponse(String message) {
        this.message = message;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
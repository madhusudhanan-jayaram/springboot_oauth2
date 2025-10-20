package com.example.demooauth2headers.controller;

import com.example.demooauth2headers.dto.AuthResponse;
import com.example.demooauth2headers.dto.LoginRequest;
import com.example.demooauth2headers.dto.RefreshRequest;
import com.example.demooauth2headers.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, 
                         UserDetailsService userDetailsService,
                         PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                            HttpServletResponse response) {
        try {
            // Load user and verify password
            var userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            
            if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
                return ResponseEntity.status(401).body(new AuthResponse("Invalid credentials"));
            }
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(loginRequest.getUsername());
            String refreshToken = jwtService.generateRefreshToken(loginRequest.getUsername());
            
            // Set custom headers
            response.setHeader("X-Access-Token", accessToken);
            response.setHeader("X-Refresh-Token", refreshToken);
            
            return ResponseEntity.ok(new AuthResponse("login ok"));
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new AuthResponse("Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest refreshRequest,
                                              HttpServletResponse response) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            
            // Validate refresh token
            if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).body(new AuthResponse("Invalid refresh token"));
            }
            
            // Get username and generate new access token
            String username = jwtService.getUsernameFromToken(refreshToken);
            String newAccessToken = jwtService.generateAccessToken(username);
            
            // Set new access token header
            response.setHeader("X-Access-Token", newAccessToken);
            
            return ResponseEntity.ok(new AuthResponse("refreshed"));
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new AuthResponse("Invalid refresh token"));
        }
    }
}
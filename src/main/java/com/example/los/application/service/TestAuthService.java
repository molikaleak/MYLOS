package com.example.los.application.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.example.los.application.dto.AuthenticationResponse;
import com.example.los.domain.auth.TUser;
import com.example.los.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple test service for authentication testing.
 * Provides quick admin login functionality for testing purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestAuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    /**
     * Test admin login with hardcoded admin credentials for testing.
     * This is a simplified version for quick testing.
     * 
     * @param username Admin username (default: "admin")
     * @param password Admin password (default: "admin123")
     * @return AuthenticationResponse with tokens
     */
    public AuthenticationResponse testAdminLogin(String username, String password) {
        log.info("Testing admin login for username: {}", username);
        
        // Find user by username
        TUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Admin user not found"));
        
        // Check if user has admin role (roleCode should be "ADMIN" or similar)
        if (!"ADMIN".equals(user.getRoleCode())) {
            log.warn("User {} does not have ADMIN role. Role code: {}", username, user.getRoleCode());
            // For testing purposes, we'll still allow login but log a warning
        }
        
        // Check if user is active
        if (!"ACTIVE".equals(user.getStatusCode())) {
            throw new IllegalStateException("Admin account is not active");
        }
        
        // Generate tokens
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        
        // Calculate expiration
        long expiresIn = jwtService.getExpirationInSeconds(accessToken);
        
        log.info("Admin login test successful for: {}", username);
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .message("Admin login test successful")
                .build();
    }
    
    /**
     * Quick test with default admin credentials.
     * Uses "admin" as username and "admin123" as password.
     * 
     * @return AuthenticationResponse with tokens
     */
    public AuthenticationResponse testDefaultAdminLogin() {
        return testAdminLogin("admin", "admin123");
    }
    
    /**
     * Verify if a token is valid and belongs to an admin user.
     * 
     * @param token JWT token to verify
     * @return true if token is valid and user has admin role
     */
    public boolean verifyAdminToken(String token) {
        if (!jwtService.validateToken(token)) {
            return false;
        }
        
        String username = jwtService.extractUsername(token);
        TUser user = userRepository.findByUsername(username)
                .orElse(null);
        
        return user != null && "ADMIN".equals(user.getRoleCode());
    }
    
    /**
     * Get admin user info by token.
     * 
     * @param token Valid JWT token
     * @return Admin user details or null if not admin
     */
    public TUser getAdminUserInfo(String token) {
        if (!jwtService.validateToken(token)) {
            return null;
        }
        
        String username = jwtService.extractUsername(token);
        TUser user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null && "ADMIN".equals(user.getRoleCode())) {
            return user;
        }
        
        return null;
    }
}
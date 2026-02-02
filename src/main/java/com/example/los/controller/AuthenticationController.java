package com.example.los.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.los.application.dto.AuthenticationResponse;
import com.example.los.application.dto.LoginRequest;
import com.example.los.application.dto.RefreshTokenRequest;
import com.example.los.application.dto.RegisterRequest;
import com.example.los.application.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            AuthenticationResponse response = authenticationService.register(registerRequest);
            log.info("User registered successfully: {}", registerRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthenticationResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthenticationResponse.builder()
                            .message("Registration failed due to server error")
                            .build());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthenticationResponse response = authenticationService.authenticate(loginRequest);
            log.info("User logged in successfully: {}", loginRequest.getUsernameOrEmail());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for: {}", loginRequest.getUsernameOrEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                            .message("Invalid credentials")
                            .build());
        } catch (IllegalStateException e) {
            log.warn("Inactive account attempt: {}", loginRequest.getUsernameOrEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthenticationResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            AuthenticationResponse response = authenticationService.refreshToken(refreshTokenRequest);
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Invalid refresh token attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                            .message("Invalid or expired refresh token")
                            .build());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest refreshTokenRequest,
                                      HttpServletRequest request) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        String accessToken = extractAccessToken(request);
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            // If no refresh token provided, we can still blacklist the access token
            if (accessToken != null) {
                authenticationService.blacklistAccessToken(accessToken);
                log.info("Access token blacklisted for logout");
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            authenticationService.logout(refreshToken, accessToken);
            log.info("User logged out successfully with token blacklisting");
        }
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Extract access token from Authorization header.
     */
    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is running");
    }
}
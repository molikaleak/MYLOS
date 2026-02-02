package com.example.los.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.los.application.dto.AuthenticationResponse;
import com.example.los.application.dto.LoginRequest;
import com.example.los.application.dto.RefreshTokenRequest;
import com.example.los.application.dto.RegisterRequest;
import com.example.los.domain.auth.TUser;
import com.example.los.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        // Create new user
        TUser user = new TUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setBranchId(registerRequest.getBranchId());
        user.setRoleCode(registerRequest.getRoleCode());
        user.setStatusCode("ACTIVE");
        user.setCreatedAt(Instant.now());
        
        // Save user
        userRepository.save(user);
        log.info("User registered successfully: {}", username);
        
        // Generate tokens for immediate login
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        
        // Save refresh token to user
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plus(7, ChronoUnit.DAYS));
        userRepository.save(user);
        
        long expiresIn = jwtService.getExpirationInSeconds(accessToken);
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .message("Registration successful")
                .build();
    }
    
    @Transactional
    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        String usernameOrEmail = loginRequest.getUsernameOrEmail();
        String password = loginRequest.getPassword();
        
        // Find user by username or email
        Optional<TUser> userOptional = userRepository.findByUsername(usernameOrEmail);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(usernameOrEmail);
        }
        
        TUser user = userOptional.orElseThrow(() ->
            new BadCredentialsException("Invalid username/email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
        
        // Check if user is active
        if (!"ACTIVE".equals(user.getStatusCode())) {
            throw new IllegalStateException("User account is not active");
        }
        
        // Generate tokens
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        
        // Save refresh token to user
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plus(7, ChronoUnit.DAYS));
        userRepository.save(user);
        
        // Calculate expiration in seconds
        long expiresIn = jwtService.getExpirationInSeconds(accessToken);
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .message("Login successful")
                .build();
    }
    
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        // Validate refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        // Find user by refresh token
        TUser user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        
        // Check if refresh token is expired (based on database expiry)
        if (user.getRefreshTokenExpiry() == null || 
            user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }
        
        // Generate new tokens
        String newAccessToken = jwtService.generateToken(user.getUsername());
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());
        
        // Update refresh token in database
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(Instant.now().plus(7, ChronoUnit.DAYS));
        userRepository.save(user);
        
        long expiresIn = jwtService.getExpirationInSeconds(newAccessToken);
        
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(expiresIn)
                .message("Token refreshed successfully")
                .build();
    }
    
    @Transactional
    public void logout(String refreshToken, String accessToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return;
        }
        
        // Find user by refresh token and clear it
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            log.info("User {} logged out successfully", user.getUsername());
        });
        
        // Blacklist the access token if provided
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtService.blacklistToken(accessToken);
            log.debug("Access token blacklisted for logout");
        }
    }
    
    @Transactional
    public void logoutByUsername(String username, String accessToken) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            log.info("User {} logged out successfully", username);
        });
        
        // Blacklist the access token if provided
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtService.blacklistToken(accessToken);
            log.debug("Access token blacklisted for logout");
        }
    }
    
    /**
     * Blacklist a specific access token (useful for token revocation).
     */
    public void blacklistAccessToken(String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtService.blacklistToken(accessToken);
            log.info("Access token blacklisted explicitly");
        }
    }
}
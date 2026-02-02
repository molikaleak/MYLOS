package com.example.los.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT configuration properties loaded from application.properties or environment variables.
 * Includes validation and production safety checks.
 */
@Slf4j
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secretKey;
    
    private Duration accessTokenExpiration = Duration.ofMinutes(30);
    
    private Duration refreshTokenExpiration = Duration.ofDays(7);
    
    private String issuer = "loan-origination-system";
    
    private String audience = "loan-origination-client";
    
    // Additional security properties
    private String algorithm = "HS256";
    private boolean validateIssuer = true;
    private boolean validateAudience = true;
    private int clockSkewSeconds = 30;
    
    /**
     * Validate JWT configuration on application startup.
     * Checks for production safety and logs warnings for insecure configurations.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("JWT Configuration loaded:");
        log.info("  Issuer: {}", issuer);
        log.info("  Audience: {}", audience);
        log.info("  Access Token Expiration: {} minutes", accessTokenExpiration.toMinutes());
        log.info("  Refresh Token Expiration: {} days", refreshTokenExpiration.toDays());
        log.info("  Algorithm: {}", algorithm);
        
        // Check for default secret key in production
        if (isDefaultSecretKey() && isProductionEnvironment()) {
            String errorMessage = """
                ⚠️ SECURITY WARNING ⚠️
                Default JWT secret key detected in production environment!
                This is a critical security risk.
                
                To fix this:
                1. Set the JWT_SECRET_KEY environment variable
                2. Use a strong, random secret key (min 32 characters)
                3. Never use default keys in production
                
                Example: export JWT_SECRET_KEY=$(openssl rand -base64 32)
                """;
            log.error(errorMessage);
            throw new IllegalStateException("Default JWT secret key detected in production environment");
        }
        
        if (isDefaultSecretKey()) {
            log.warn("⚠️ Using default JWT secret key. This should be changed for production.");
            log.warn("   Set JWT_SECRET_KEY environment variable with a strong random key.");
        }
        
        // Validate secret key strength
        validateSecretKeyStrength();
        
        // Validate expiration times
        validateExpirationTimes();
    }
    
    /**
     * Check if the current environment is production.
     */
    private boolean isProductionEnvironment() {
        String env = System.getProperty("APP_ENVIRONMENT",
                       System.getenv().getOrDefault("APP_ENVIRONMENT", "development"));
        return "production".equalsIgnoreCase(env) || "prod".equalsIgnoreCase(env);
    }
    
    /**
     * Check if using the default secret key.
     */
    private boolean isDefaultSecretKey() {
        if (secretKey == null) {
            return true;
        }
        
        String[] defaultKeys = {
            "your-256-bit-secret-key-change-this-in-production",
            "secret",
            "changeme",
            "default",
            "test"
        };
        
        String lowerKey = secretKey.toLowerCase();
        for (String defaultKey : defaultKeys) {
            if (lowerKey.contains(defaultKey)) {
                return true;
            }
        }
        
        return secretKey.length() < 32;
    }
    
    /**
     * Validate secret key strength.
     */
    private void validateSecretKeyStrength() {
        if (secretKey == null) {
            log.error("JWT secret key is null");
            return;
        }
        
        int length = secretKey.length();
        
        if (length < 32) {
            log.warn("JWT secret key is only {} characters. Recommended minimum is 32 characters.", length);
        } else if (length >= 32 && length < 64) {
            log.info("JWT secret key length: {} characters (acceptable)", length);
        } else {
            log.info("JWT secret key length: {} characters (strong)", length);
        }
        
        // Check for weak patterns
        if (secretKey.matches("^[a-zA-Z]+$")) {
            log.warn("JWT secret key contains only letters. Consider adding numbers and special characters.");
        }
    }
    
    /**
     * Validate token expiration times.
     */
    private void validateExpirationTimes() {
        if (accessTokenExpiration.toMinutes() > 60) {
            log.warn("Access token expiration is {} minutes. Consider shorter durations (15-30 minutes) for better security.",
                     accessTokenExpiration.toMinutes());
        }
        
        if (refreshTokenExpiration.toDays() > 30) {
            log.warn("Refresh token expiration is {} days. Consider shorter durations (7-30 days) for better security.",
                     refreshTokenExpiration.toDays());
        }
    }
    
    /**
     * Get the secret key bytes for JWT signing.
     */
    public byte[] getSecretKeyBytes() {
        if (secretKey == null) {
            throw new IllegalStateException("JWT secret key is not configured");
        }
        return secretKey.getBytes();
    }
    
    /**
     * Check if configuration is valid for production use.
     */
    public boolean isProductionReady() {
        return !isDefaultSecretKey() &&
               secretKey.length() >= 32 &&
               accessTokenExpiration.toMinutes() <= 60 &&
               refreshTokenExpiration.toDays() <= 30;
    }
    
    /**
     * Get configuration summary for logging (without exposing secret).
     */
    public String getConfigurationSummary() {
        return String.format(
            "JWT Configuration: issuer=%s, audience=%s, accessExp=%dm, refreshExp=%dd, productionReady=%s",
            issuer, audience,
            accessTokenExpiration.toMinutes(),
            refreshTokenExpiration.toDays(),
            isProductionReady()
        );
    }
}
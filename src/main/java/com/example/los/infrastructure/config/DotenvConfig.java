package com.example.los.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@Configuration
public class DotenvConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DotenvConfig.class);
    
    private final Environment environment;
    
    public DotenvConfig(Environment environment) {
        this.environment = environment;
    }
    
    @PostConstruct
    public void init() {
        String activeProfile = getActiveProfile();
        String envFileName = determineEnvFileName(activeProfile);
        
        logger.info("Loading environment variables for profile: {}", activeProfile);
        logger.info("Looking for .env file: {}", envFileName);
        
        try {
            Dotenv dotenv = Dotenv.configure()
                .filename(envFileName)
                .ignoreIfMissing()
                .load();
            
            int loadedCount = 0;
            for (var entry : dotenv.entries()) {
                String key = entry.getKey();
                String value = maskSensitiveValue(key, entry.getValue());
                
                // Only set if not already present as system property
                if (System.getProperty(key) == null) {
                    System.setProperty(key, entry.getValue());
                    loadedCount++;
                    logger.debug("Loaded environment variable: {} = {}", key, value);
                } else {
                    logger.debug("Skipping {} (already set as system property)", key);
                }
            }
            
            logger.info("Successfully loaded {} environment variables from {}", loadedCount, envFileName);
            
            // Log important configuration values (masked)
            logConfigurationSummary();
            
        } catch (Exception e) {
            logger.warn("Failed to load .env file: {}. Using system environment variables only.", e.getMessage());
            logger.debug("Detailed error:", e);
        }
    }
    
    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles[0];
        }
        
        String defaultProfile = environment.getProperty("spring.profiles.active", "default");
        return defaultProfile;
    }
    
    private String determineEnvFileName(String profile) {
        // Map Spring profiles to .env file names
        return switch (profile.toLowerCase()) {
            case "prod", "production" -> ".env.production";
            case "dev", "development" -> ".env.development";
            case "test", "testing" -> ".env.test";
            case "staging" -> ".env.staging";
            case "local" -> ".env.local";
            default -> ".env";
        };
    }
    
    private String maskSensitiveValue(String key, String value) {
        if (value == null) {
            return "null";
        }
        
        // Mask sensitive values in logs
        if (key.toLowerCase().contains("password") || 
            key.toLowerCase().contains("secret") || 
            key.toLowerCase().contains("key") ||
            key.toLowerCase().contains("token")) {
            
            if (value.length() <= 4) {
                return "****";
            }
            return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        }
        
        // For other values, show first few chars only
        if (value.length() > 20) {
            return value.substring(0, 10) + "...";
        }
        
        return value;
    }
    
    private void logConfigurationSummary() {
        String[] importantKeys = {
            "DATABASE_URL",
            "DATABASE_USERNAME",
            "JWT_SECRET_KEY",
            "SERVER_PORT",
            "APP_ENVIRONMENT",
            "REDIS_HOST",
            "KAFKA_BOOTSTRAP_SERVERS"
        };
        
        logger.info("=== Configuration Summary ===");
        for (String key : importantKeys) {
            String value = System.getProperty(key);
            if (value != null) {
                logger.info("{} = {}", key, maskSensitiveValue(key, value));
            }
        }
        logger.info("============================");
    }
    
    /**
     * Utility method to get an environment variable with fallback.
     * This can be used throughout the application to access environment variables.
     */
    public static String getEnv(String key, String defaultValue) {
        // Check system properties first (set by DotenvConfig)
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        
        // Check environment variables
        value = System.getenv(key);
        if (value != null) {
            return value;
        }
        
        return defaultValue;
    }
    
    /**
     * Utility method to get an environment variable or throw exception if missing.
     */
    public static String getRequiredEnv(String key) {
        String value = getEnv(key, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable '" + key + "' is not set");
        }
        return value;
    }
    
    /**
     * Check if we're running in production environment.
     */
    public static boolean isProduction() {
        String env = getEnv("APP_ENVIRONMENT", "development");
        return "production".equalsIgnoreCase(env) || "prod".equalsIgnoreCase(env);
    }
    
    /**
     * Check if we're running in development environment.
     */
    public static boolean isDevelopment() {
        String env = getEnv("APP_ENVIRONMENT", "development");
        return "development".equalsIgnoreCase(env) || "dev".equalsIgnoreCase(env) || "local".equalsIgnoreCase(env);
    }
}
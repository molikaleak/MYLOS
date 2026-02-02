package com.example.los.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.los.infrastructure.config.DotenvConfig;
import com.example.los.infrastructure.config.JwtProperties;

/**
 * Test class to verify environment configuration loading.
 * This test demonstrates how environment variables are loaded from .env files
 * and used in the application configuration.
 */
@SpringBootTest
@ActiveProfiles("local")
class EnvironmentConfigTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void testJwtPropertiesLoadedFromEnvironment() {
        // Verify JWT properties are loaded
        assertThat(jwtProperties).isNotNull();
        assertThat(jwtProperties.getIssuer()).isEqualTo("loan-origination-local");
        assertThat(jwtProperties.getAudience()).isEqualTo("loan-origination-local-client");
        
        // Verify secret key is loaded from environment (not default)
        assertThat(jwtProperties.getSecretKey())
            .isNotBlank()
            .isNotEqualTo("your-256-bit-secret-key-change-this-in-production");
        
        // Verify production safety check
        assertThat(jwtProperties.isProductionReady()).isFalse(); // Should be false for local/test
    }

    @Test
    void testDotenvConfigUtilityMethods() {
        // Test environment variable retrieval with fallback
        String dbUrl = DotenvConfig.getEnv("DATABASE_URL", "default-url");
        assertThat(dbUrl).isEqualTo("jdbc:postgresql://localhost:5432/loan_db_local");
        
        String nonExistent = DotenvConfig.getEnv("NON_EXISTENT_VAR", "default-value");
        assertThat(nonExistent).isEqualTo("default-value");
        
        // Test environment detection
        assertThat(DotenvConfig.isDevelopment()).isTrue();
        assertThat(DotenvConfig.isProduction()).isFalse();
    }

    @Test
    void testConfigurationPriority() {
        // This test demonstrates the configuration priority:
        // 1. System environment variables (highest)
        // 2. .env file values
        // 3. application.properties defaults (lowest)
        
        // The JWT secret should come from .env.local file
        String secretKey = jwtProperties.getSecretKey();
        assertThat(secretKey).contains("test-local-secret-key");
        
        // Server port should come from .env.local (8081, not 8080)
        // Note: We can't directly test server.port here without Spring context
        // but the configuration is loaded via environment variables
    }

    @Test
    void testEnvironmentSpecificConfiguration() {
        // Verify that we're using the local profile
        String appEnvironment = DotenvConfig.getEnv("APP_ENVIRONMENT", "unknown");
        assertThat(appEnvironment).isEqualTo("local");
        
        // Verify debug mode is enabled for local
        String appDebug = DotenvConfig.getEnv("APP_DEBUG", "false");
        assertThat(appDebug).isEqualTo("true");
    }
}
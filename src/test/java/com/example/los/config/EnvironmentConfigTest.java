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
@ActiveProfiles("test")
class EnvironmentConfigTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void testJwtPropertiesLoadedFromEnvironment() {
        // Verify JWT properties are loaded
        assertThat(jwtProperties).isNotNull();
        assertThat(jwtProperties.getIssuer()).isEqualTo("loan-origination-local");
        assertThat(jwtProperties.getAudience()).isEqualTo("loan-origination-local-client");
        
        // Verify secret key is loaded from test configuration
        assertThat(jwtProperties.getSecretKey())
            .isNotBlank()
            .contains("test-local-secret-key");
        
        // Verify production safety check
        assertThat(jwtProperties.isProductionReady()).isFalse(); // Should be false for test
    }

    @Test
    void testDotenvConfigUtilityMethods() {
        // Test environment variable retrieval with fallback
        String dbUrl = DotenvConfig.getEnv("DATABASE_URL", "default-url");
        assertThat(dbUrl).isEqualTo("jdbc:h2:mem:testdb");
        
        String nonExistent = DotenvConfig.getEnv("NON_EXISTENT_VAR", "default-value");
        assertThat(nonExistent).isEqualTo("default-value");
        
        // Test environment detection - test environment should not be development or production
        assertThat(DotenvConfig.isDevelopment()).isFalse();
        assertThat(DotenvConfig.isProduction()).isFalse();
        // Verify APP_ENVIRONMENT is "test"
        String appEnv = DotenvConfig.getEnv("APP_ENVIRONMENT", "unknown");
        assertThat(appEnv).isEqualTo("test");
    }

    @Test
    void testConfigurationPriority() {
        // This test demonstrates the configuration priority:
        // 1. System environment variables (highest)
        // 2. .env file values
        // 3. application.properties defaults (lowest)
        
        // The JWT secret should come from .env.test file
        String secretKey = jwtProperties.getSecretKey();
        assertThat(secretKey).contains("test-local-secret-key");
        
        // Server port should come from .env.test (8081, not 8080)
        // Note: We can't directly test server.port here without Spring context
        // but the configuration is loaded via environment variables
    }

    @Test
    void testEnvironmentSpecificConfiguration() {
        // Verify that we're using the test profile
        String appEnvironment = DotenvConfig.getEnv("APP_ENVIRONMENT", "unknown");
        assertThat(appEnvironment).isEqualTo("test");
        
        // Verify debug mode is enabled for test
        String appDebug = DotenvConfig.getEnv("APP_DEBUG", "false");
        assertThat(appDebug).isEqualTo("true");
    }
}
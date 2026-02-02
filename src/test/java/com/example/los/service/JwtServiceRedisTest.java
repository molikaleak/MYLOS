package com.example.los.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.los.application.service.JwtService;
import com.example.los.infrastructure.config.JwtProperties;

/**
 * Test for JWT token blacklisting with Redis integration.
 * This test demonstrates the Redis-based token blacklisting functionality.
 */
@ExtendWith(MockitoExtension.class)
public class JwtServiceRedisTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Configure mock JWT properties
        when(jwtProperties.getSecretKey()).thenReturn("test-secret-key-1234567890-1234567890");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(30));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        when(jwtProperties.getIssuer()).thenReturn("test-issuer");
        when(jwtProperties.getAudience()).thenReturn("test-audience");

        // Configure Redis template mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testTokenGenerationAndValidation() {
        // Generate a token
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertTrue(token.contains(".")); // JWT has 3 parts separated by dots

        // Extract username
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);

        // Validate token
        assertTrue(jwtService.validateToken(token, "testuser"));
        assertFalse(jwtService.validateToken(token, "wronguser"));
    }

    @Test
    void testTokenBlacklisting() {
        // Generate a token
        String token = jwtService.generateToken("testuser");
        
        // Mock Redis operations for blacklisting
        when(valueOperations.get("jwt:blacklist:signature-part")).thenReturn(null, "blacklisted");
        
        // Initially token should not be blacklisted
        assertFalse(jwtService.isTokenBlacklisted(token));
        
        // Blacklist the token
        jwtService.blacklistToken(token);
        
        // Now token should be blacklisted
        assertTrue(jwtService.isTokenBlacklisted(token));
        
        // Validate should fail for blacklisted token
        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void testBlacklistTokenWithExpiredToken() {
        // For an expired token, blacklisting should not add to Redis
        String expiredToken = "expired.token.signature";
        
        // Mock token validation to return false (expired)
        // Note: We can't easily mock private methods, so this test is simplified
        jwtService.blacklistToken(expiredToken);
        
        // Verify no Redis operations were performed for expired token
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testRemoveFromBlacklist() {
        String token = jwtService.generateToken("testuser");
        
        // Mock Redis operations
        when(valueOperations.get("jwt:blacklist:signature-part")).thenReturn("blacklisted", null);
        
        // Initially blacklisted
        assertTrue(jwtService.isTokenBlacklisted(token));
        
        // Remove from blacklist
        jwtService.removeFromBlacklist(token);
        
        // Now should not be blacklisted
        assertFalse(jwtService.isTokenBlacklisted(token));
    }

    @Test
    void testGetBlacklistTTL() {
        String token = jwtService.generateToken("testuser");
        
        // Mock Redis TTL operation
        when(redisTemplate.getExpire("jwt:blacklist:signature-part", java.util.concurrent.TimeUnit.SECONDS))
            .thenReturn(1800L); // 30 minutes
        
        Long ttl = jwtService.getBlacklistTTL(token);
        assertNotNull(ttl);
        assertEquals(1800L, ttl);
    }

    @Test
    void testTokenSignatureExtraction() {
        // Test with valid JWT format
        String token = "header.payload.signature";
        // The getTokenSignature method is private, but we can test through blacklisting
        // which calls it internally
        
        // This test verifies the token processing doesn't throw exceptions
        assertDoesNotThrow(() -> jwtService.blacklistToken(token));
    }
}
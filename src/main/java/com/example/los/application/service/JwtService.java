package com.example.los.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.los.infrastructure.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;
    
    // Redis key prefix for blacklisted tokens
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(String username) {
        return generateToken(username, jwtProperties.getAccessTokenExpiration(), "access");
    }
    
    public String generateRefreshToken(String username) {
        return generateToken(username, jwtProperties.getRefreshTokenExpiration(), "refresh");
    }
    
    private String generateToken(String username, java.time.Duration expiration, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType);
        
        Instant now = Instant.now();
        Instant expiry = now.plus(expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token) && !isTokenBlacklisted(token));
    }
    
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token) && !isTokenBlacklisted(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public long getExpirationInSeconds(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        return (expiration.getTime() - now.getTime()) / 1000;
    }
    
    /**
     * Blacklist a token by adding it to Redis with TTL equal to token expiration.
     * This ensures the token cannot be used even if it's still valid.
     */
    public void blacklistToken(String token) {
        try {
            if (!validateToken(token)) {
                log.warn("Attempted to blacklist invalid or expired token");
                return;
            }
            
            String tokenSignature = getTokenSignature(token);
            long ttlSeconds = getExpirationInSeconds(token);
            
            if (ttlSeconds > 0) {
                String blacklistKey = BLACKLIST_KEY_PREFIX + tokenSignature;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
                log.debug("Token blacklisted with TTL: {} seconds", ttlSeconds);
            } else {
                log.warn("Token already expired, not adding to blacklist");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check if a token is blacklisted in Redis.
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenSignature = getTokenSignature(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenSignature;
            String value = redisTemplate.opsForValue().get(blacklistKey);
            return value != null && value.equals("blacklisted");
        } catch (Exception e) {
            log.error("Error checking token blacklist: {}", e.getMessage(), e);
            // If Redis is unavailable, assume token is not blacklisted to avoid blocking users
            return false;
        }
    }
    
    /**
     * Extract the signature part of the JWT token for use as Redis key.
     * JWT format: header.payload.signature
     */
    private String getTokenSignature(String token) {
        try {
            // Simple approach: use the last part of the token (signature)
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                return parts[2];
            }
            // Fallback: use the whole token as key
            return token;
        } catch (Exception e) {
            log.warn("Failed to extract token signature, using full token as key");
            return token;
        }
    }
    
    /**
     * Get remaining TTL for a blacklisted token.
     */
    public Long getBlacklistTTL(String token) {
        try {
            String tokenSignature = getTokenSignature(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenSignature;
            return redisTemplate.getExpire(blacklistKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting blacklist TTL: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Remove a token from blacklist (useful for testing or admin operations).
     */
    public void removeFromBlacklist(String token) {
        try {
            String tokenSignature = getTokenSignature(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenSignature;
            redisTemplate.delete(blacklistKey);
            log.debug("Token removed from blacklist");
        } catch (Exception e) {
            log.error("Error removing token from blacklist: {}", e.getMessage(), e);
        }
    }
}